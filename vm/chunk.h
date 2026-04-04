#ifndef clox_name_h
#define clox_name_h

#include "common.h"
#include "value.h"

typedef enum{
    OP_RETURN,
    OP_CONSTANT,
    OP_NIL,
    OP_TRUE,
    OP_FALSE,
    OP_ADD,
    OP_SUBTRACT,
    OP_MULTIPLY,
    OP_DIVIDE,
    OP_NEGATE,
    OP_NOT,
    OP_EQUAL,
    OP_GREATER,
    OP_LESS
} OpCode;

typedef struct{
    int count; //number of allocated bytes that are actually in use
    int capacity; //Total allocated space i.e. how many bytes we can store before resizing
    uint8_t* code; //pointer to a dynamically allocated array of bytes, each byte represents a piece of instruction i.e bytecode
    int* lines;
    ValueArray constants; //
} Chunk;

void initChunk(Chunk* chunk);
void writeChunk(Chunk* chunk, uint8_t byte, int line);
void freeChunk(Chunk* chunk);
int addConstant(Chunk* chunk, Value value);

#endif