#include <stdio.h>
#include <assert.h>
#include "sphinxbase/err.h"

static FILE* log_fp = NULL;

int logging_start(char const *log_file)
{
  assert(log_fp == NULL);
  if (log_file && log_file[0] != 0) {
    if ((log_fp = fopen(log_file, "a")) == NULL) {
      E_ERROR("Failed to open logfile %s\n", log_file);
      return -1;
    }
    err_set_logfp(log_fp);
  }
  return 0;
}

/* For other than main threads */
void logging_set_fp()
{
  if (log_fp == NULL)
    return;

  if (err_get_logfp() != log_fp)
    err_set_logfp(log_fp);
}

void logging_stop()
{
  if (log_fp != NULL) {
    fclose(log_fp);
    log_fp = NULL;
  }
}
