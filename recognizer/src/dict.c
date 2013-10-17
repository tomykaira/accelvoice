#include <assert.h>

#include "dict.h"
#include "t2p.h"

#define HASH_TABLE_SIZE 1024

static hash_table_t *inserted_words = NULL;
static int word_id = 0;

void dict_start()
{
  inserted_words = hash_table_new(HASH_TABLE_SIZE, HASH_CASE_NO);
  word_id = 0;
}

void dict_stop()
{
  hash_table_free(inserted_words);
  inserted_words = NULL;
  word_id = 0;
}

/* acronym := unknown AND word length <= 3 */
static int is_acronym(char *word)
{
  return strlen(word) <= 3;
}

#define ADD_IF(char, phone) case char: \
  strcat(buffer, phone " "); \
  break;

static char *synthesize_acronym_phones(const char *word)
{
  char buffer[128] = "";
  int index = 0;

  while (word[index]) {
    switch (word[index]) {
      ADD_IF('A', "EY")
      ADD_IF('B', "B IY")
      ADD_IF('C', "S IY")
      ADD_IF('D', "D IY")
      ADD_IF('E', "IY")
      ADD_IF('F', "EH F")
      ADD_IF('G', "JH IY")
      ADD_IF('H', "EY CH")
      ADD_IF('I', "AY")
      ADD_IF('J', "JH EY")
      ADD_IF('K', "K EY")
      ADD_IF('L', "EH L")
      ADD_IF('M', "EH M")
      ADD_IF('N', "EH N")
      ADD_IF('O', "OW")
      ADD_IF('P', "P IY")
      ADD_IF('Q', "K Y UW")
      ADD_IF('R', "AA R")
      ADD_IF('S', "EH S")
      ADD_IF('T', "T IY")
      ADD_IF('U', "Y UW")
      ADD_IF('V', "V IY")
      ADD_IF('W', "D AH B AH L Y UW")
      ADD_IF('X', "EH K S")
      ADD_IF('Y', "W AY")
      ADD_IF('Z', "Z IY")
    }
    index++;
  }

  return ckd_salloc(buffer);
}

static int insert_word(ps_decoder_t *ps, char *word, char*phones)
{
  if (hash_table_enter_int32(inserted_words, ckd_salloc(word), word_id) != word_id)
    return 0;

  ++word_id;
  fprintf(stderr, "%s -> \"%s\"\n", word, phones);

  /*
    If update == TRUE, ps_add_word() calls search_reinit.
    It is called later in recognize(), so not necessary here.
  */
  if (ps_add_word(ps, word, phones, FALSE) < 0) {
    fprintf(stderr, "Failed to add word %s\n", word);
    return -1;
  }
  return 0;
}

int insert_unknown_words(ps_decoder_t *ps, char *unknown[])
{
  assert(ps != NULL);
  assert(unknown != NULL);

  for (int i = 0; unknown[i] != NULL; ++i) {
    char * word = unknown[i];
    char * phones;

    phones = synthesize_phones(word);
    if (insert_word(ps, word, phones) == -1) {
      free(phones);
      return -1;
    }
    free(phones);

    if (is_acronym(word)) {
      char *word2 = ckd_calloc(strlen(word) + 4, sizeof(char));
      phones = synthesize_acronym_phones(word);
      sprintf(word2, "%s(2)", word);
      if (insert_word(ps, word2, phones) == -1) {
        free(phones);
        ckd_free(word2);
        return -1;
      }
      ckd_free(word2);
      ckd_free(phones);
    }
  }

  return 0;
}
