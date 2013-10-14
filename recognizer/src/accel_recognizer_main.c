#include <stdio.h>
#include <unistd.h>

#include "../include/accel_recognizer.h"

int main(int argc, char *argv[])
{
  char *query[] = {
    "queue",
    "arrayToCharSequence",
    "require",
    NULL
  };

  start(argc, argv);

  printf("%s\n", recognize(query));

  stop();
  return 0;
}
