package com.shimmerresearch.hil;

import static org.junit.Assume.assumeTrue;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.junit.After;
import org.junit.Assert;
import org.junit.Test;

import com.shimmerresearch.bluetooth.ShimmerBluetooth;
import com.shimmerresearch.bluetooth.ShimmerBluetooth.BT_CRC_MODE;
import com.shimmerresearch.bluetooth.ShimmerBluetooth.BT_STATE;

/**
 * Hardware-in-the-loop scenarios for the Java driver: a real Shimmer3 or Shimmer3R on a paired
 * Bluetooth serial port, driven through ShimmerPC with its timers on, as Consensys and MATLAB run
 * it. Run by tools/hil/run-java-hil.ps1 through the hilTest task, one scenario per JVM. Without
 * hil.port every test is skipped, so the class never runs (or fails) without hardware.
 *
 * <pre>
 *   hil.port            COM port of the unit (required)
 *   hil.mac, hil.unit   for the report
 *   hil.type            Shimmer3 (default) or Shimmer3R
 *   hil.rate            sampling rate in Hz, default 256
 *   hil.seconds         streamRest duration, default 30
 *   hil.cycles          midStreamSet / reconnectLoop / inStream repetitions, default 3 / 5 / 3
 *   hil.overlaps        inStream overlap rounds, default 5
 *   hil.crc             off, 1 or 2; default: the driver's own default
 *   hil.maxLossPercent  default 1.0
 *   hil.outDir          where reports go, default build/hil
 * </pre>
 */
public class JavaHilScenariosTest {

	private static final double GRAVITY = 9.81;
	/** At 256 Hz a second without a packet is ~256 packets the gap count cannot see. */
	private static final double MAX_SILENCE_AT_END_MS = 1000;
	/** The ACK/response timer's own line when a command gets no reply: "Command:	<NAME> timeout". */
	private static final java.util.regex.Pattern COMMAND_TIMEOUT = java.util.regex.Pattern.compile("Command:\t\\S+ timeout");

	private HilSession session;
	private HilReport report;
	private HilShimmerPC device;
	private int logStart;

	private static String arg(String name, String fallback) {
		String v = System.getProperty("hil." + name);
		return v == null || v.isEmpty() ? fallback : v;
	}

	private static double arg(String name, double fallback) {
		return Double.parseDouble(arg(name, Double.toString(fallback)));
	}

	private static int arg(String name, int fallback) {
		return Integer.parseInt(arg(name, Integer.toString(fallback)));
	}

	private void begin(String scenario) {
		String port = arg("port", null);
		assumeTrue("No unit given: pass -Phil.port=COMx (tools/hil/run-java-hil.ps1 does)", port != null);
		String crc = arg("crc", null);
		if (crc != null) {
			ShimmerBluetooth.setDefaultBtCrcModeToUseIfFwSupported(
					"off".equals(crc) ? BT_CRC_MODE.OFF : "1".equals(crc) ? BT_CRC_MODE.ONE_BYTE_CRC : BT_CRC_MODE.TWO_BYTE_CRC);
		}
		Map<String, String> args = new LinkedHashMap<>();
		System.getProperties().stringPropertyNames().stream().filter(k -> k.startsWith("hil.")).sorted()
				.forEach(k -> args.put(k.substring(4), System.getProperty(k)));
		String mac = arg("mac", "");
		String type = arg("type", "Shimmer3");
		report = new HilReport(scenario, arg("unit", port), port, mac, type, args);
		session = new HilSession(port, mac, type);
		device = session.device;
		logStart = device.logSize();
	}

	@After
	public void end() {
		if (session == null) {
			return;
		}
		putDriverCounts();
		session.close();
		report.write(new File(arg("outDir", "build/hil")), session.events(), device.log());
	}

	private void finish() {
		if (session.hung != null) {
			report.put("hungCall", session.hung);
			report.check(false, session.hung + " never returned: the API call blocked (thread dump in the report's events)");
		}
		if (!report.failures().isEmpty()) {
			Assert.fail(String.join("; ", report.failures()));
		}
	}

	/** What the driver did in this session: replies by type, decodes, timer requests, connects. */
	private void putDriverCounts() {
		report.put("connects", session.connects);
		report.put("inStreamReplies", device.inStreamReplies());
		report.put("statusesDecoded", device.statusesDecoded());
		report.put("batteryReadings", device.batteryReadings());
		report.put("statusRequests", device.countSince(logStart, "GET_STATUS_COMMAND Instruction added"));
		report.put("commandTimeouts", device.countSince(logStart, COMMAND_TIMEOUT));
	}

	// --- shared checks -------------------------------------------------------------------------

	private boolean connectAndConfigure(double rateHz) throws Exception {
		long start = System.currentTimeMillis();
		boolean connected = session.connect(30000);
		report.put("connectMs", System.currentTimeMillis() - start);
		report.check(connected, "did not connect and initialise within 30 s (state " + session.state() + ")");
		if (!connected) {
			return false;
		}
		report.put("hardware", device.getHardwareVersionParsed());
		report.put("firmware", device.getFirmwareVersionParsed());
		report.put("crcMode", String.valueOf(device.getCurrentBtCommsCrcMode()));
		boolean configured = session.configureAccelOnly(rateHz, 20000);
		report.put("rateOnDeviceHz", device.getSamplingRateShimmer());
		report.check(configured, "configuration did not complete within 20 s (state " + session.state() + ")");
		return configured;
	}

	private void checkRate(double deviceRateHz, String what) {
		report.check(!Double.isNaN(deviceRateHz) && deviceRateHz > 0,
				what + ": the API has no sampling rate for the unit (" + deviceRateHz + " Hz)");
	}

	private void checkStream(StreamAnalysis a, List<StreamAnalysis.Sample> samples, double stoppedAtMs,
			double maxLossPercent, double deviceRateHz, String what) {
		report.check(a.received > 0, what + ": no packets received");
		report.check(a.lossPercent() <= maxLossPercent, String.format(Locale.ROOT,
				"%s: %.3f%% packets lost (%d of %d), limit %.2f%%", what, a.lossPercent(), a.missing,
				a.received + a.missing, maxLossPercent));
		report.check(a.nonIncreasing == 0, what + ": " + a.nonIncreasing + " timestamps did not increase");
		checkJumps(a, what);
		checkTimeline(a, samples, stoppedAtMs, deviceRateHz, what);
	}

	/** A device timestamp that leapt ahead of the arrival time is wrong data (DEV-1023 class), never loss. */
	private void checkJumps(StreamAnalysis a, String what) {
		for (StreamAnalysis.Gap j : a.timestampJumps) {
			report.check(false, String.format(Locale.ROOT,
					"%s: timestamp jumped %.1f s ahead at %.0f ms (device time %.0f ms) while packets kept arriving",
					what, (j.missing + 1) * a.expectedPeriodMs / 1000.0, j.afterReceivedMs, j.afterTimestampMs));
		}
	}

	/**
	 * Loss is counted between packets received, so two things it cannot see are checked here: the
	 * timestamps must give a rate within 1 % of the configured one, and packets must have kept
	 * arriving until recording stopped.
	 */
	private void checkTimeline(StreamAnalysis a, List<StreamAnalysis.Sample> samples, double stoppedAtMs,
			double deviceRateHz, String what) {
		if (Double.isNaN(a.effectiveRateHz) || Double.isInfinite(a.effectiveRateHz)) {
			report.check(false, what + ": no usable device timestamps to measure the rate from (" + a.received + " packets)");
		} else {
			report.check(Math.abs(a.effectiveRateHz - deviceRateHz) <= 0.01 * deviceRateHz, String.format(Locale.ROOT,
					"%s: effective rate %.2f Hz, device set to %.2f Hz", what, a.effectiveRateHz, deviceRateHz));
		}
		if (!samples.isEmpty()) {
			double silentMs = stoppedAtMs - samples.get(samples.size() - 1).receivedMs;
			report.check(silentMs <= MAX_SILENCE_AT_END_MS, String.format(Locale.ROOT,
					"%s: no packet in the last %.1f s before recording stopped (the stream went silent)", what, silentMs / 1000.0));
		}
	}

	/** Parser errors and command timeouts the driver logged since index from. */
	private void checkDriverLog(int from, String what) {
		int parserErrors = device.countSince(from, "Unknown parsing error", "Unknown packet error", "Throw Bytes");
		report.check(parserErrors == 0, what + ": " + parserErrors + " parser errors logged by the driver (see the .driver.log)");
		int timeouts = device.countSince(from, COMMAND_TIMEOUT);
		report.check(timeouts == 0, what + ": " + timeouts + " command timeouts logged by the driver (see the .driver.log)");
	}

	private static double[] toArray(List<Double> values) {
		double[] out = new double[values.size()];
		for (int i = 0; i < out.length; i++) {
			out[i] = values.get(i);
		}
		return out;
	}

	// --- scenarios -----------------------------------------------------------------------------

	/** Connect, configure, stream at rest, stop, disconnect. */
	@Test
	public void streamRest() throws Exception {
		begin("stream_rest");
		double rate = arg("rate", 256.0);
		int seconds = arg("seconds", 30);
		double maxLoss = arg("maxLossPercent", 1.0);
		if (connectAndConfigure(rate)) {
			double deviceRate = device.getSamplingRateShimmer();
			checkRate(deviceRate, "stream");
			int logMark = device.logSize();
			session.startRecording();
			report.check(session.startStreaming(10000), "streaming did not start within 10 s");
			Thread.sleep(seconds * 1000L);
			double stoppedAtMs = session.nowMs();
			List<StreamAnalysis.Sample> samples = session.stopRecording();
			report.check(session.stopStreaming(10000), "streaming did not stop within 10 s");
			StreamAnalysis a = StreamAnalysis.analyse(samples, deviceRate);
			session.event("stream: " + a);
			report.putStream("stream", a);
			report.putGaps(a);
			report.samples(samples);
			checkStream(a, samples, stoppedAtMs, maxLoss, deviceRate, "stream");
			if (!Double.isNaN(a.accelMagnitudeMean)) {
				report.check(Math.abs(a.accelMagnitudeMean - GRAVITY) <= 1.0, String.format(Locale.ROOT,
						"accel at rest %.3f m/s^2, expected %.2f±1.00 (is the unit lying still?)", a.accelMagnitudeMean, GRAVITY));
			}
			checkDriverLog(logMark, "stream");
			report.check(session.disconnect(10000), "did not disconnect within 10 s");
		}
		finish();
	}

	/**
	 * Commands while streaming: start and stop SD logging (SET_RWC and the logging commands, with
	 * their bare ACKs) and read the battery, each waited on. Needs an SD card in the unit.
	 */
	@Test
	public void midStreamSet() throws Exception {
		begin("mid_stream_set");
		double rate = arg("rate", 256.0);
		int cycles = arg("cycles", 3);
		double maxLoss = arg("maxLossPercent", 1.0);
		if (connectAndConfigure(rate)) {
			double deviceRate = device.getSamplingRateShimmer();
			checkRate(deviceRate, "stream");
			int logMark = device.logSize();
			session.startRecording();
			report.check(session.startStreaming(10000), "streaming did not start within 10 s");
			Thread.sleep(3000);
			List<Double> sdMoments = new ArrayList<>();
			List<Double> batteryMoments = new ArrayList<>();
			for (int i = 1; i <= cycles && session.hung == null; i++) {
				session.event("cycle " + i);
				sdMoments.add(session.nowMs());
				boolean started = session.call("startSDLogging", 10000, device::startSDLogging);
				report.check(started, "cycle " + i + ": startSDLogging did not complete: it threw, hung or was skipped (see the events)");
				if (started) {
					report.check(session.waitFor(() -> session.state() == BT_STATE.STREAMING_AND_SDLOGGING, 5000),
							"cycle " + i + ": SD logging did not start while streaming (state " + session.state() + "; is there an SD card?)");
				}
				Thread.sleep(2000);
				sdMoments.add(session.nowMs());
				boolean stopped = session.call("stopSDLogging", 10000, device::stopSDLogging);
				report.check(stopped, "cycle " + i + ": stopSDLogging did not complete: it threw, hung or was skipped (see the events)");
				if (stopped) {
					report.check(session.waitFor(() -> session.state() == BT_STATE.STREAMING, 5000),
							"cycle " + i + ": SD logging did not stop (state " + session.state() + ")");
				}
				Thread.sleep(1000);
				batteryMoments.add(session.nowMs());
				int readingsBefore = device.batteryReadings();
				boolean asked = session.call("readBattery", 10000, device::readBattery);
				report.check(asked, "cycle " + i + ": readBattery did not complete: it threw, hung or was skipped (see the events)");
				if (asked) {
					report.check(session.waitFor(() -> device.batteryReadings() > readingsBefore, 5000),
							"cycle " + i + ": no battery reply decoded within 5 s of readBattery");
				}
				Thread.sleep(2000);
			}
			double stoppedAtMs = session.nowMs();
			List<StreamAnalysis.Sample> samples = session.stopRecording();
			report.check(session.stopStreaming(10000), "streaming did not stop within 10 s");
			StreamAnalysis a = StreamAnalysis.analyse(samples, deviceRate);
			session.event("stream: " + a);
			report.putStream("stream", a);
			report.putGaps(a);
			report.samples(samples);
			// Loss around SD logging start/stop is reported, not failed: the unit itself pauses while it
			// opens or closes its file. Loss anywhere else is the driver's.
			int[] attributed = a.attribute(new double[][] {toArray(sdMoments), toArray(batteryMoments)}, 1000);
			report.put("missingNearSdLoggingStartStop", attributed[0]);
			report.put("missingNearBatteryReads", attributed[1]);
			if (attributed[0] > 0) {
				session.event(attributed[0] + " packets not received within 1 s of SD logging start/stop (firmware side; not failed)");
			}
			report.check(attributed[1] == 0, attributed[1] + " packets lost within 1 s of a battery read while streaming");
			int elsewhere = a.missing - attributed[0];
			double elsewherePercent = 100.0 * elsewhere / Math.max(1, a.received + a.missing);
			report.put("missingElsewhere", elsewhere);
			report.check(elsewherePercent <= maxLoss, String.format(Locale.ROOT,
					"stream: %.3f%% packets lost away from SD logging start/stop (%d), limit %.2f%%", elsewherePercent, elsewhere, maxLoss));
			report.check(a.received > 0, "stream: no packets received");
			report.check(a.nonIncreasing == 0, "stream: " + a.nonIncreasing + " timestamps did not increase");
			checkJumps(a, "stream");
			checkTimeline(a, samples, stoppedAtMs, deviceRate, "stream");
			checkDriverLog(logMark, "stream");
			report.check(session.disconnect(10000), "did not disconnect within 10 s");
		}
		finish();
	}

	/** Connect, stream for 5 s and disconnect, repeatedly. Every connect starts the status timer. */
	@Test
	public void reconnectLoop() throws Exception {
		begin("reconnect_loop");
		double rate = arg("rate", 256.0);
		int cycles = arg("cycles", 5);
		double maxLoss = arg("maxLossPercent", 1.0);
		List<Long> connectTimes = new ArrayList<>();
		for (int i = 1; i <= cycles && session.hung == null; i++) {
			String what = "cycle " + i;
			long start = System.currentTimeMillis();
			boolean ready = i == 1 ? connectAndConfigure(rate) : session.connect(30000);
			connectTimes.add(System.currentTimeMillis() - start);
			report.check(ready, what + ": did not connect within 30 s (state " + session.state() + ")");
			if (!ready) {
				break;
			}
			double deviceRate = device.getSamplingRateShimmer();
			checkRate(deviceRate, what);
			int logMark = device.logSize();
			session.startRecording();
			report.check(session.startStreaming(10000), what + ": streaming did not start within 10 s");
			Thread.sleep(5000);
			double stoppedAtMs = session.nowMs();
			List<StreamAnalysis.Sample> samples = session.stopRecording();
			report.check(session.stopStreaming(10000), what + ": streaming did not stop within 10 s");
			StreamAnalysis a = StreamAnalysis.analyse(samples, deviceRate);
			session.event(what + " stream: " + a);
			checkStream(a, samples, stoppedAtMs, maxLoss, deviceRate, what);
			checkDriverLog(logMark, what);
			report.check(session.disconnect(10000), what + ": did not disconnect within 10 s");
			Thread.sleep(2000);
		}
		report.put("connectMs", connectTimes);
		finish();
	}

	/**
	 * In-stream commands while streaming. First each reply on its own: status (0x8A 0x71), directory
	 * (0x8A 0x88) and battery (0x8A 0x94), each checked to be decoded. Then the overlap: SET_RWC
	 * followed at once by status and battery requests, so a reply arrives while another command
	 * waits for its ACK (DEV-1262). Every command must complete, with its own reply, and none may
	 * time out. Packets the unit drops right after an RTC write are reported; timestamp faults fail.
	 */
	@Test
	public void inStream() throws Exception {
		begin("in_stream");
		double rate = arg("rate", 256.0);
		int rounds = arg("cycles", 3);
		int overlaps = arg("overlaps", 5);
		double maxLoss = arg("maxLossPercent", 1.0);
		if (connectAndConfigure(rate)) {
			double deviceRate = device.getSamplingRateShimmer();
			checkRate(deviceRate, "stream");
			int logMark = device.logSize();
			session.startRecording();
			report.check(session.startStreaming(10000), "streaming did not start within 10 s");
			Thread.sleep(2000);
			for (int i = 1; i <= rounds && session.hung == null; i++) {
				String what = "round " + i;
				int statusBefore = device.statusesDecoded();
				expectReply(what, "status", "STATUS", device::readStatusLogAndStream, () -> device.statusesDecoded() > statusBefore);
				expectReply(what, "directory", "DIR", device::readDirectoryName,
						() -> device.getDirectoryName() != null && !device.getDirectoryName().isEmpty());
				int batteryBefore = device.batteryReadings();
				expectReply(what, "battery", "VBATT", device::readBattery, () -> device.batteryReadings() > batteryBefore);
				Thread.sleep(500);
			}
			List<Double> rtcWrites = new ArrayList<>();
			for (int i = 1; i <= overlaps && session.hung == null; i++) {
				String what = "overlap " + i;
				rtcWrites.add(session.nowMs());
				int statusReplies = device.inStreamReplies("STATUS");
				int batteryReplies = device.inStreamReplies("VBATT");
				int statusBefore = device.statusesDecoded();
				int batteryBefore = device.batteryReadings();
				int timeoutsBefore = device.countSince(logMark, COMMAND_TIMEOUT);
				boolean sent = session.call(what + ": SET_RWC, GET_STATUS and GET_VBATT back to back", 10000, () -> {
					device.writeRealTimeClock();
					device.readStatusLogAndStream();
					device.readBattery();
				});
				report.check(sent, what + ": the three calls did not complete (see the events)");
				if (sent) {
					boolean done = session.waitFor(() -> device.inStreamReplies("STATUS") > statusReplies
							&& device.inStreamReplies("VBATT") > batteryReplies
							&& session.queuedInstructions() == 0, 5000);
					report.check(done, String.format(Locale.ROOT,
							"%s: not every command completed within 5 s (status replies +%d, battery replies +%d, %d still queued)",
							what, device.inStreamReplies("STATUS") - statusReplies, device.inStreamReplies("VBATT") - batteryReplies,
							session.queuedInstructions()));
					report.check(device.statusesDecoded() > statusBefore, what + ": the status reply was not decoded");
					report.check(device.batteryReadings() > batteryBefore, what + ": the battery reply was not decoded");
					int timeouts = device.countSince(logMark, COMMAND_TIMEOUT) - timeoutsBefore;
					report.check(timeouts == 0, what + ": " + timeouts + " command timeouts (a reply matched to the wrong command?)");
				}
				Thread.sleep(1000);
			}
			double stoppedAtMs = session.nowMs();
			List<StreamAnalysis.Sample> samples = session.stopRecording();
			report.check(session.stopStreaming(10000), "streaming did not stop within 10 s");
			report.check(session.waitFor(() -> session.state() == BT_STATE.CONNECTED, 3000),
					"after streaming the unit is " + session.state() + ", not CONNECTED");
			StreamAnalysis a = StreamAnalysis.analyse(samples, deviceRate);
			session.event("stream: " + a);
			report.putStream("stream", a);
			report.putGaps(a);
			report.samples(samples);
			// Loss right after an RTC write is reported, not failed: the unit itself drops packets when
			// its clock is set while streaming (firmware side, as at SD logging start in midStreamSet).
			// Timestamps that go backwards or leap ahead still fail: the API hands them to apps.
			int nearRtc = a.attribute(new double[][] {toArray(rtcWrites)}, 1000)[0];
			report.put("missingNearRtcWrites", nearRtc);
			if (nearRtc > 0) {
				session.event(nearRtc + " packets not received within 1 s of an RTC write (firmware side; not failed)");
			}
			int elsewhere = a.missing - nearRtc;
			double elsewherePercent = 100.0 * elsewhere / Math.max(1, a.received + a.missing);
			report.put("missingElsewhere", elsewhere);
			report.check(elsewherePercent <= maxLoss, String.format(Locale.ROOT,
					"stream: %.3f%% packets lost away from the RTC writes (%d), limit %.2f%%", elsewherePercent, elsewhere, maxLoss));
			report.check(a.received > 0, "stream: no packets received");
			report.check(a.nonIncreasing == 0, "stream: " + a.nonIncreasing + " timestamps did not increase");
			checkJumps(a, "stream");
			checkTimeline(a, samples, stoppedAtMs, deviceRate, "stream");
			checkDriverLog(logMark, "stream");
			report.check(session.disconnect(10000), "did not disconnect within 10 s");
		}
		finish();
	}

	/** Sends one in-stream request and checks its reply both arrived and was decoded within 3 s. */
	private void expectReply(String round, String what, String replyType, HilSession.Call request,
			HilSession.Condition decoded) throws InterruptedException {
		int before = device.inStreamReplies(replyType);
		if (!session.call(round + ": request " + what, 10000, request)) {
			report.check(false, round + ": the " + what + " request did not complete (see the events)");
			return;
		}
		boolean arrived = session.waitFor(() -> device.inStreamReplies(replyType) > before, 3000);
		report.check(arrived, round + ": no in-stream " + what + " reply within 3 s");
		if (arrived) {
			report.check(session.waitFor(decoded, 1000), round + ": the " + what + " reply arrived but was not decoded");
		}
	}
}
