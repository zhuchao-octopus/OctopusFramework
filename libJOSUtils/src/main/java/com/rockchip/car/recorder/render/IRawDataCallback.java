package com.rockchip.car.recorder.render;

public interface IRawDataCallback {
	public byte[] fetchRawData();
	public byte[] fetchYData();
	public byte[] fetchUVData();
	public byte[] fetchUData();
	public byte[] fetchVData();
	public void notifyTextureUpdated(byte[] data);
}
