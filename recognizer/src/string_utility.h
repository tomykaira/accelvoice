#ifndef _STRING_UTILITY_H_
#define _STRING_UTILITY_H_

enum WordFormat
{
  DOWNCASE,
  UPCASE,
  AS_IS
};

char * apply_format(char *str, enum WordFormat format);
char ** list_words(const char *token, enum WordFormat format);

#endif /* _STRING_UTILITY_H_ */
