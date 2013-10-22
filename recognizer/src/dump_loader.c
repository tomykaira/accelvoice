#include <string.h>
#include <stdlib.h>
#include <assert.h>

#include "pocketsphinx.h"
#include "dump_loader.h"

char ***loaded_query;
char **loaded_unknown;

typedef struct s_list {
  void *value;
  struct s_list *next;
} list;

static list * new_list()
{
  return ckd_calloc(1, sizeof(list));
}

static void free_list(list *l)
{
  if (l == NULL)
    return;
  free_list(l->next);
  l->next = NULL;
  ckd_free(l);
}

static int list_length(list *l)
{
  int i = 0;
  while (l->value != NULL && l->next != NULL) {
    l = l->next;
    i++;
  }
  return i;
}

static void** to_array_list(list *l)
{
  int length = list_length(l);
  void **result = ckd_calloc(length + 1, sizeof(void *));
  int ptr = 0;
  while (l->value && l->next) {
    result[ptr] = l->value;
    ptr++;
    l = l->next;
  }
  assert(ptr == length);
  return result;
}

/*
  candidate words
  candidate words
  candidate words

  unknown
  unknown
 */
int load_from_file(FILE *fp)
{
  list *candidate_list = new_list();
  list *candidate_cur = candidate_list;
  list *unknown_list = new_list();
  list *unknown_cur = unknown_list;
  char buf[1024];

  while (1) {
    int ptr = 0;

    fgets(buf, 1024, fp);
    if (buf[0] == '\n')
      break;
    list *head = new_list();
    list *cur = head;
    int start = 0;
    while (buf[ptr] != '\0') {
      while (buf[ptr] != ' ' && buf[ptr] != '\n') {
        ptr++;
      }
      cur->value = ckd_calloc(ptr - start + 1, 1);
      strncpy(cur->value, buf + start, ptr - start);
      cur->next = new_list();
      cur = cur->next;
      start = ptr + 1;
      ptr++;
    }
    candidate_cur->value = to_array_list(head);
    free_list(head);
    candidate_cur->next = new_list();
    candidate_cur = candidate_cur->next;
  }

  while (fgets(buf, 1024, fp) != NULL) {
    if (buf[0] == '\n' || buf[0] == '\0')
      break;
    buf[strlen(buf) - 1] = '\0';
    unknown_cur->value = ckd_salloc(buf);
    unknown_cur->next = new_list();
    unknown_cur = unknown_cur->next;
  }

  loaded_query = (char ***)to_array_list(candidate_list);
  loaded_unknown = (char **)to_array_list(unknown_list);

  return 0;
}
