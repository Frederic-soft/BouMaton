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
