import pyxdf
import pandas as pd

# Step 1: Load the XDF file
xdf_file_path = 'C:\\Users\\XX\\Documents\\CurrentStudy\\sub-P001\\ses-S001\\eeg\\sub-P001_ses-S001_task-Default_run-001_eeg.xdf'
streams, header = pyxdf.load_xdf(xdf_file_path)


# Iterate through the streams and save each as a CSV
for idx, stream in enumerate(streams):
    # Extract data and timestamps
    data = stream['time_series']
    timestamps = stream['time_stamps']
    
    # Attempt to retrieve channel names, fallback to generic names if not available
    try:
        channels = [ch['label'][0] for ch in stream['info']['desc'][0]['channels'][0]['channel']]
    except (KeyError, TypeError, IndexError):
        channels = [f'channel_{i}' for i in range(data.shape[1])]
    
    # Retrieve the stream name
    try:
        stream_name = stream['info']['name'][0]
    except (KeyError, TypeError, IndexError):
        stream_name = f'stream_{idx+1}'
    
    # Create a DataFrame
    print(channels)
    print(data)
    df = pd.DataFrame(data, columns=channels)
    df['time_stamps'] = timestamps

    # Save DataFrame to CSV using the stream name
    csv_file_path = f'{stream_name}.csv'
    df.to_csv(csv_file_path, index=False)
    print(f"Stream {idx+1} saved to {csv_file_path}")