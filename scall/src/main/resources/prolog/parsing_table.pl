% this file provides the theory used to compute the parsing table starting from
% a knowledge base consisting of declared productions, declared terminals, a start symbol.
% the following functors are to be used in the knowledge base:
% start_symbol(Nonterminal).
% production(Head, Body).
% terminal(Terminal).

% first_str(String, Entry)
% relates a string of symbols with an entry in its FIRST set
% the ε entry (empty string) is represented with the number 0
first_str([], 0).
first_str([A | _], A) :- terminal(A).
first_str([X | _], A) :- first(X, A), A \== 0.
first_str([X | T], A) :- first(X, 0), first_str(T, A).

% first(Nonterminal, Entry)
% relates a nonterminal with an entry in its FIRST set
% the ε entry is represented with the number 0
first(X, A) :- production(X, B), first_str(B, A).

% following(Nonterminal, String, Head)
% relates a nonterminal with a string of symbols that follows it within a production body
% along with the head of that same production
following(X, S, H) :- production(H, B), append(_, [X | S], B), \+ terminal(X).

% follow(Nonterminal, Entry)
% relates a nonterminal with an entry in its FOLLOW set
% the $ entry (end of input) is represented with the number 1
follow(X, 1) :- start_symbol(X).
follow(X, A) :- following(X, S, _), first_str(S, A), A \== 0.
follow(X, A) :- following(X, S, H), H \== X, first_str(S, 0), follow(H, A).

% parsing_cell(Nonterminal, Terminal, Body)
% relates a position within the parsing table, given by a nonterminal along with a terminal
% (possibly end of input), with the value in that cell, being a production body
% assuming the starting grammar is LL(1), each cell will have only one possible value
parsing_cell(X, A, B) :- production(X, B), first_str(B, A), A \== 0.
parsing_cell(X, A, B) :- production(X, B), first_str(B, 0), follow(X, A).
