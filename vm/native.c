#include <time.h>
#include <string.h>
#include <stdio.h>
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

void registerNatives() {
    defineNative("clock", clockNative);
    defineNative("input", inputNative);
}