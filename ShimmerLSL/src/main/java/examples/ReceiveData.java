package examples;

import edu.ucsd.sccn.LSL;

public class ReceiveData {
    public static void main(String[] args) throws Exception  {
        
        NativeLibraryLoader.loadLibrary(); //to use the lib/liblsl64.dll
        try {
            System.out.println("Resolving an Accel stream from Com5...");
            LSL.StreamInfo[] typeResults = LSL.resolve_stream("type", "Accel");
            LSL.StreamInfo[] nameResults = LSL.resolve_stream("name", "SendData_Device1_Com5"); // Replace "SendData_Device1_Com5" with the name of your outlet
            
            LSL.StreamInfo[] results = combineResults(typeResults, nameResults);

            // open an inlet
            LSL.StreamInlet inlet = new LSL.StreamInlet(results[0]);
            
            // receive data
            float[] sample = new float[inlet.info().channel_count()];
            while (true) {
                inlet.pull_sample(sample);
                for (int k=0;k<sample.length;k++)
                    System.out.print("\t" + Double.toString(sample[k]));
                System.out.println();
            }
        } catch(Exception ex) {
            ex.printStackTrace();
        }
    }

    private static LSL.StreamInfo[] combineResults(LSL.StreamInfo[] arr1, LSL.StreamInfo[] arr2) {
        int length = arr1.length + arr2.length;
        LSL.StreamInfo[] result = new LSL.StreamInfo[length];
        System.arraycopy(arr1, 0, result, 0, arr1.length);
        System.arraycopy(arr2, 0, result, arr1.length, arr2.length);
        return result;
    }
}
