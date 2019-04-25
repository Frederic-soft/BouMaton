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
#include <stdlib.h>

typedef struct {
  int red;
  int green;
  int blue;
} Point;

#define WIDTH 320
#define HEIGHT 240

typedef Point Column[HEIGHT];
typedef Column Image[WIDTH];

Image * boulanger(Image img, long n) {   
  // Create a new image to store the result
  Image * res = malloc(sizeof(Image));
  int x, y, i;
  
  for (y = 0; y < HEIGHT; y++) {  // for each line
    for (x = 0; x < WIDTH; x++) { // for each column
      int nx = x;   // compute the new position of the current point
      int ny = y;
      
      // When n > 0, we apply the direct transform
      if (n > 0) {
        for (i = 0; i < n; i++) {
          nx = 2 * nx + ny % 2;
          ny = (ny - ny % 2) / 2;
          if (nx >= WIDTH) {
            ny =  HEIGHT - 1 - ny;
            nx = 2 * WIDTH - nx - 1;
          }
        }
      } else {
        // When n < 0, we apply the reverse transform
        for (i = 0; i < -n; i++) {
          ny = 2 * ny + nx % 2;
          nx = (nx - nx % 2) / 2;
          if (ny >= HEIGHT) {
            nx =  WIDTH - 1 - nx;
            ny = 2 * HEIGHT - ny - 1;
          }
        }
      }
      // Get the color of point (x,y) in the image, and set
      // point (nx,ny) to the same color in the result.
      (*res)[nx][ny] = img[x][y];
    }
  }
  return res;
}
