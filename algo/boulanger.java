/*
 *  BouMaton, a program to compute transformations on pictures.
 *  Copyright (C) 2019  Frédéric Boulanger (frederic.boulanger@centralesupelec.fr)
 * 
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 * 
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 * 
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <https://www.gnu.org/licenses/>.
 * 
 */
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
