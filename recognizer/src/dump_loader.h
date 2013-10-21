#ifndef _DUMP_LOADER_H_
#define _DUMP_LOADER_H_

#include <stdio.h>

extern char ***loaded_query;
extern char **loaded_unknown;

int load_from_file(FILE *fp);

#endif /* _DUMP_LOADER_H_ */
