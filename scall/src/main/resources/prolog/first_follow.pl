first_str([], 0). % 0 indicates ε, string is nullable
first_str([A | _], A) :- terminal(A).
first_str([X | _], A) :- first(X, A), A \== 0.
first_str([X | T], A) :- first(X, 0), first_str(T, A).

first(X, A) :- production(X, P), first_str(P, A).

follow(S, 1) :- start_symbol(S). % 1 indicates $, end of file
follow(X, A) :- following(X, B, _), first_str(B, A), A \== 0.
follow(X, A) :- following(X, B, H), H \== X, first_str(B, 0), follow(H, A).
