#include <assert.h>

#include "dict.h"
#include "t2p.h"

#define HASH_TABLE_SIZE 1024

static hash_table_t *inserted_words = NULL;
static int word_id = 0;

int insert_unknown_words(ps_decoder_t *ps, char *unknown[])
{
  assert(ps != NULL);
  assert(unknown != NULL);
  if (inserted_words == NULL) {
    inserted_words = hash_table_new(HASH_TABLE_SIZE, HASH_CASE_NO);
  }

  for (int i = 0; unknown[i] != NULL; ++i) {
    char * word = unknown[i];
    char * phones;

    if (hash_table_enter_int32(inserted_words, ckd_salloc(word), word_id) != word_id)
      continue;
    ++word_id;

    phones = synthesize_phones(word);
    fprintf(stderr, "%s -> \"%s\"\n", word, phones);

    /*
      If update == TRUE, ps_add_word() calls search_reinit.
      It is called later in recognize(), so not necessary here.
    */
    if (ps_add_word(ps, word, phones, FALSE) < 0) {
      fprintf(stderr, "Failed to add word %s\n", unknown[i]);
      free(phones);
      return -1;
    }
    free(phones);
  }

  return 0;
}
