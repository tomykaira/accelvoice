#include <string.h>
#include <stdlib.h>
#include <assert.h>
#include <ctype.h>

#include "string_utility.h"

char * apply_format(char *str, enum WordFormat format)
{
  switch (format) {
  case AS_IS:
    break;
  case DOWNCASE:
    for (int i = 0; i < strlen(str); ++i) {
      str[i] = tolower(str[i]);
    }
    break;
  case UPCASE:
    for (int i = 0; i < strlen(str); ++i) {
      str[i] = toupper(str[i]);
    }
    break;
  }
  return str;
}

char ** list_words(const char *token, enum WordFormat format)
{
  uint length = strlen(token);
  char buffer[1024];
  char result[128][1024];
  uint buffer_ptr = 0, result_ptr = 0;

  for (int i = 0; i < length; ++i) {
    char c = token[i];
    char next = i + 1 < length ? token[i + 1] : 0;

    if (islower(c) || (isupper(c) && !islower(next))) {
      buffer[buffer_ptr++] = c;
    } else if (isdigit(c) || (isupper(c) && islower(next))) {
      if (buffer_ptr > 0) {
        buffer[buffer_ptr++] = 0;
        strcpy(result[result_ptr++], apply_format(buffer, format));
      }
      buffer_ptr = 0;
      buffer[buffer_ptr++] = c;
    } else if (buffer_ptr > 0) {
      if (buffer_ptr > 0) {
        buffer[buffer_ptr++] = 0;
        strcpy(result[result_ptr++], apply_format(buffer, format));
      }
      buffer_ptr = 0;
    }

    assert(buffer_ptr < 1024);
    assert(result_ptr < 128);
  }

  if (buffer_ptr > 0) {
    buffer[buffer_ptr++] = 0;
    strcpy(result[result_ptr++], apply_format(buffer, format));
  }

  // copy to heap
  char **ret = (char **) malloc((result_ptr + 1) * sizeof(char*));
  for (int i = 0; i < result_ptr; ++i) {
    ret[i] = malloc((strlen(result[i]) + 1) * sizeof(char));
    strcpy(ret[i], result[i]);
  }
  ret[result_ptr] = NULL;
  return ret;
}
