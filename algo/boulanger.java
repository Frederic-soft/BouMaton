package algo;

import java.awt.image.BufferedImage;

class Boulanger {
	/**
	 * Apply the baker's transform <code>n</code> to <code>img</code>
	 * and return the resulting image.
	 */
	@SuppressWarnings("unused")
	private BufferedImage boulanger(BufferedImage img, long n) {   
		int width = img.getWidth();
		int height = img.getHeight();
		// Create a new image to store the result
		BufferedImage res = new BufferedImage(width, height, img.getType());

		for (int y = 0; y < height; y++) {  // for each line
			for (int x = 0; x < width; x++) { // for each column
				int nx = x;   // compute the new position of the current point
				int ny = y;

				// When n > 0, we apply the direct transform
				if (n > 0) {
					for (int i = 0; i < n; i++) {
						nx = 2 * nx + ny % 2;
						ny = (ny - ny % 2) / 2;
						if (nx >= width) {
							ny =  height - 1 - ny;
							nx = 2 * width - nx - 1;
						}
					}
				} else {
					// When n < 0, we apply the reverse transform
					for (int i = 0; i < -n; i++) {
						ny = 2 * ny + nx % 2;
						nx = (nx - nx % 2) / 2;
						if (ny >= height) {
							nx =  width - 1 - nx;
							ny = 2 * height - ny - 1;
						}
					}
				}
				// Get the color of point (x,y) in the image, and set
				// point (nx,ny) to the same color in the result.
				res.setRGB(nx, ny, img.getRGB(x, y));
			}
		}
		return res;
	}

}
