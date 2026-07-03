package gay.sylv.polycog.api.client.wheel.image;

/// The pixel format that a [GpuImage] uses.
public enum ImageFormat {
	/// 32-bit SRGB with 8 bits per component.
	///
	/// `0xBBGGRRAA`
	BGRA32_SRGB(false);

	private final boolean hdr;

	ImageFormat(boolean hdr) {
		this.hdr = hdr;
	}

	/// Whether this format supports HDR.
	public boolean isHdr() {
		return this.hdr;
	}
}
