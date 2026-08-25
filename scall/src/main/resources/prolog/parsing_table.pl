first_str([], 0). % 0 indicates ε, string is nullable
first_str([A | _], A) :- terminal(A).
first_str([X | _], A) :- first(X, A), A \== 0.
first_str([X | T], A) :- first(X, 0), first_str(T, A).

first(X, A) :- production(X, P), first_str(P, A).

following(X, S, H) :- production(H, P), append(_, [X | S], P), \+ terminal(X).

follow(X, 1) :- start_symbol(X). % 1 indicates $, end of file
follow(X, A) :- following(X, S, _), first_str(S, A), A \== 0.
follow(X, A) :- following(X, S, H), H \== X, first_str(S, 0), follow(H, A).

parsing_cell(X, A, B) :- production(X, B), first_str(B, A), A \== 0.
parsing_cell(X, A, B) :- production(X, B), first_str(B, 0), follow(X, A).
