; this is a comment
COPY     START   0
FIRST    MOV     %ELX,RETADR  ; save Return Address
         MOV     #LENGTH,%EBX
         BASE    LENGTH
CLOOP   +JSUB    RDREC
         MOV     LENGTH,%EAX
         COMP    #0
         JEQ     ENDFIL
        +JSUB    WRREC
         J       CLOOP
ENDFIL   MOV     EOF,%EAX
         MOV     %EAX,BUFFER
         MOV     #3,%EAX
         MOV     %EAX,LENGTH
        +JSUB    WRREC
         J       @RETADR      ; Return back to the caller
EOF      BYTE    C'EOF'
RETADR   RESW    1
LENGTH   RESW    1
BUFFER   RESB    4096
RDREC    CLEAR   %EXX
         CLEAR   %EAX
         CLEAR   %ESX
        +MOV     #4096,%ETX
RLOOP    TD      INPUT
         JEQ     RLOOP
         RD      INPUT
         COMPR   %ESX,%EAX
         JEQ     EXIT
         STCH    BUFFER[%EXX]
         TIXR    %ETX
         JLT     RLOOP
EXIT     MOV     %EXX,LENGTH
         RSUB
INPUT    BYTE    X'F3'
WRREC    CLEAR   %EXX
         MOV     LENGTH,%ETX
WLOOP    TD      OUTPUT
         JEQ     WLOOP
         LDCH    BUFFER[%EXX]
         WD      OUTPUT
         TIXR    %ETX
         JLT     WLOOP
         RSUB
OUTPUT   BYTE    X'05'
         END     FIRST

