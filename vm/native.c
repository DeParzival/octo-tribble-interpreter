#include <time.h>
#include <string.h>
#include <stdio.h>
#include <stdlib.h>
#include "native.h"
#include "vm.h"
#include "object.h"
#include "value.h"

static Value clockNative(int argCount, Value* args) {
    return NUMBER_VAL((double)clock() / CLOCKS_PER_SEC);
}

static Value inputNative(int argCount, Value* args) {
    char buffer[1024];
    
    // Read line from standard input
    if (fgets(buffer, sizeof(buffer), stdin) == NULL) {
        return NIL_VAL; // Return nil if EOF or error occurs
    }

    // Strip trailing newline character (\n) if present
    buffer[strcspn(buffer, "\n")] = '\0';

    // Copy string to the heap and return it as an ObjString Value
    return OBJ_VAL(copyString(buffer, (int)strlen(buffer)));
}

static Value strNative(int argCount, Value* args) {
    if (argCount != 1) {
        return NIL_VAL; 
    }

    char buffer[64];
    if (IS_NUMBER(args[0])) {
        sprintf(buffer, "%g", AS_NUMBER(args[0]));
        return OBJ_VAL(copyString(buffer, (int)strlen(buffer)));
    } else if (IS_BOOL(args[0])) {
        const char* b = AS_BOOL(args[0]) ? "true" : "false";
        return OBJ_VAL(copyString(b, (int)strlen(b)));
    }
    return NIL_VAL; // Add more types if needed
}

static Value numNative(int argCount, Value* args) {
    if (argCount != 1 || !IS_STRING(args[0])) {
        runtimeError("num() expects 1 string argument.");
        return NIL_VAL;
    }

    ObjString* string = AS_STRING(args[0]);
    char* end;
    double value = strtod(string->chars, &end);

    // If 'end' points to the start of the string, or if there's trailing 
    // invalid junk that wasn't part of the number, flag an error.
    if (end == string->chars || *end != '\0') {
        runtimeError("Invalid number format: '%s'", string->chars);
        return NIL_VAL;
    }

    return NUMBER_VAL(value);
}

void registerNatives() {
    defineNative("clock", clockNative);
    defineNative("input", inputNative);
    defineNative("str", strNative);
    defineNative("num", numNative);
}