first_str([], []).
first_str([A | _], A) :- terminal(A).
first_str([X | _], A) :- first(X, A), A \== [].
first_str([X | T], A) :- first(X, []), first_str(T, A).

first(X, A) :- production(X, P), first_str(P, A).
