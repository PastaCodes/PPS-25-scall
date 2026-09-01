### Product Backlog

01/07/26

<table>
    <thead>
        <tr>
            <th>Priority</th>
            <th>Item</th>
            <th>Initial Size<br>Estimate</th>
            <th>Final<br>Estimate</th>
        </tr>
    </thead>
    <tbody>
        <tr>
            <td>1</td>
            <td>DSL constructs</td>
            <td>14</td>
            <td>6</td>
        </tr>
        <tr>
            <td>2</td>
            <td>Grammar conversion from EBNF to CFG</td>
            <td>12</td>
            <td>14</td>
        </tr>
        <tr>
            <td>3</td>
            <td>Lexical analysis</td>
            <td>15</td>
            <td>17</td>
        </tr>
        <tr>
            <td>4</td>
            <td>Scala-TuProlog bindings</td>
            <td>5</td>
            <td>6</td>
        </tr>
        <tr>
            <td>5</td>
            <td>Syntactical analysis</td>
            <td>30</td>
            <td>35</td>
        </tr>
        <tr>
            <td>6</td>
            <td>FINF language use case</td>
            <td>10</td>
            <td>20</td>
        </tr>
        <tr>
            <th colspan="4">Optional features</th>
        </tr>
        <tr>
            <td>7</td>
            <td>Automatic left factoring</td>
            <td>10</td>
            <td>10</td>
        </tr>
        <tr>
            <td>8</td>
            <td>CST to AST conversion constructs</td>
            <td>12</td>
            <td>17</td>
        </tr>
        <tr>
            <td>9</td>
            <td>Support for disambiguation rules</td>
            <td>15</td>
            <td>-</td>
        </tr>
        <tr>
            <td>10</td>
            <td>Extension from LL(1) to LL(*)</td>
            <td>30</td>
            <td>-</td>
        </tr>
        <tr>
            <td>11</td>
            <td>Graphical display of ASTs</td>
            <td>10</td>
            <td>-</td>
        </tr>
    </tbody>
</table>

### Sprint Backlog #1

01/07/26

<table>
    <thead>
        <tr>
            <th rowspan="2">Product Backlog Item</th>
            <th rowspan="2">Sprint Task</th>
            <th rowspan="2">Volunteer</th>
            <th rowspan="2">Initial Estimate<br>of Effort</th>
            <th colspan="5">New&nbsp;Estimates&nbsp;at&nbsp;end&nbsp;of&nbsp;Day...</th>
        </tr>
        <tr>
            <th>1</th><th>2</th><th>3</th><th>4</th><th>5</th>    
        </tr>
    </thead>
    <tbody>
        <tr>
            <td rowspan="3">DSL constructs</td>
            <td>Define the Element ADT and the EBNF operators</td>
            <td>DM</td>
            <td>2</td>
            <td>0</td><td>0</td><td>0</td><td>0</td><td>0</td>
        </tr>
        <tr>
            <td>Unify terminals and restore structural equality</td>
            <td>DM</td>
            <td>2</td>
            <td>2</td><td>3</td><td>1</td><td>0</td><td>0</td>
        </tr>
        <tr>
            <td>Infer symbol names from definition</td>
            <td>DM</td>
            <td>2</td>
            <td>2</td><td>2</td><td>1</td><td>0</td><td>0</td>
        </tr>
        <tr>
            <td rowspan="5">Grammar conversion from EBNF to CFG</td>
            <td>Process element "alternatives"</td>
            <td>MB</td>
            <td>2</td>
            <td>1</td><td>0</td><td>0</td><td>0</td><td>0</td>
        </tr>
        <tr>
            <td>Process element productions</td>
            <td>MB</td>
            <td>2</td>
            <td>1</td><td>0</td><td>0</td><td>0</td><td>0</td>
        </tr>
        <tr>
            <td>Process element "partial followings"</td>
            <td>MB</td>
            <td>2</td>
            <td>2</td><td>2</td><td>1</td><td>0</td><td>0</td>
        </tr>
        <tr>
            <td>Process element "followings"</td>
            <td>MB</td>
            <td>2</td>
            <td>2</td><td>2</td><td>1</td><td>0</td><td>0</td>
        </tr>
        <tr>
            <td>Recursive traversal of tree-like grammar</td>
            <td>MB</td>
            <td>4</td>
            <td>3</td><td>3</td><td>4</td><td>2</td><td>0</td>
        </tr>
        <tr>
            <td rowspan="4">Lexical analysis</td>
            <td>Define Token ADT and Terminal regex matching</td>
            <td>JT</td>
            <td>4</td>
            <td>2</td><td>0</td><td>0</td><td>0</td><td>0</td>
        </tr>
        <tr>
            <td>Implement LazyList.unfold state machine</td>
            <td>JT</td>
            <td>5</td>
            <td>5</td><td>3</td><td>0</td><td>0</td><td>0</td>
        </tr>
        <tr>
            <td>Longest-prefix-match tie-breaking logic</td>
            <td>JT</td>
            <td>4</td>
            <td>4</td><td>4</td><td>4</td><td>1</td><td>0</td>
        </tr>
        <tr>
            <td>Handle skipped terminals and lexical errors</td>
            <td>JT</td>
            <td>2</td>
            <td>2</td><td>2</td><td>2</td><td>2</td><td>0</td>
        </tr>
        <tr>
            <td>FINF language use case</td>
            <td>FINF grammar</td>
            <td>MB</td>
            <td>2</td>
            <td>1</td><td>1</td><td>1</td><td>1</td><td>0</td>
        </tr>
    </tbody>
</table>

### Sprint Backlog #2

18/07/26

<table>
    <thead>
        <tr>
            <th rowspan="2">Product Backlog Item</th>
            <th rowspan="2">Sprint Task</th>
            <th rowspan="2">Volunteer</th>
            <th rowspan="2">Initial Estimate<br>of Effort</th>
            <th colspan="5">New&nbsp;Estimates&nbsp;at&nbsp;end&nbsp;of&nbsp;Day...</th>
        </tr>
        <tr>
            <th>1</th><th>2</th><th>3</th><th>4</th><th>5</th>    
        </tr>
    </thead>
    <tbody>
        <tr>
            <td rowspan="2">Scala-tuProlog bindings</td>
            <td>Functional-style wrapper methods</td>
            <td>MB</td>
            <td>3</td>
            <td>1</td><td>0</td><td>0</td><td>0</td><td>0</td>
        </tr>
        <tr>
            <td>Registered objects management</td>
            <td>MB</td>
            <td>2</td>
            <td>2</td><td>0</td><td>0</td><td>0</td><td>0</td>
        </tr>
        <tr>
            <td rowspan="5">Syntactical analysis</td>
            <td>Compute FIRST and FOLLOW sets</td>
            <td>MB</td>
            <td>5</td>
            <td>5</td><td>5</td><td>2</td><td>0</td><td>0</td>
        </tr>
        <tr>
            <td>Build the parsing table</td>
            <td>MB</td>
            <td>3</td>
            <td>3</td><td>3</td><td>3</td><td>3</td><td>0</td>
        </tr>
                <tr>
            <td>Table-driven expansion and terminal matching</td>
            <td>DM</td>
            <td>5</td>
            <td>5</td><td>2</td><td>0</td><td>0</td><td>0</td>
        </tr>
        <tr>
            <td>Build the CST during the recursive descent</td>
            <td>DM</td>
            <td>4</td>
            <td>4</td><td>4</td><td>1</td><td>0</td><td>0</td>
        </tr>
        <tr>
            <td>Simplify parseSequence and extract the result alias</td>
            <td>DM</td>
            <td>3</td>
            <td>3</td><td>3</td><td>3</td><td>1</td><td>0</td>
        </tr>
        <tr>
            <td rowspan="5">CST to AST conversion</td>
            <td>Define CSTNode ADT</td>
            <td>JT</td>
            <td>2</td>
            <td>0</td><td>0</td><td>0</td><td>0</td><td>0</td>
        </tr>
        <tr>
            <td>Implement extractors for Rule and Leaf</td>
            <td>JT</td>
            <td>2</td>
            <td>2</td><td>0</td><td>0</td><td>0</td><td>0</td>
        </tr>
        <tr>
            <td>Define AstDecoder type class and monad operations</td>
            <td>JT</td>
            <td>3</td>
            <td>3</td><td>3</td><td>0</td><td>0</td><td>0</td>
        </tr>
        <tr>
            <td>Implement sequence decoding and list aggregation</td>
            <td>JT</td>
            <td>4</td>
            <td>4</td><td>4</td><td>4</td><td>2</td><td>0</td>
        </tr>
        <tr>
            <td>Define AstError ADT and fail-fast propagation</td>
            <td>JT</td>
            <td>2</td>
            <td>2</td><td>2</td><td>2</td><td>2</td><td>0</td>
        </tr>
    </tbody>
</table>

### Sprint Backlog #3

18/08/26

<table>
    <thead>
        <tr>
            <th rowspan="2">Product Backlog Item</th>
            <th rowspan="2">Sprint Task</th>
            <th rowspan="2">Volunteer</th>
            <th rowspan="2">Initial Estimate<br>of Effort</th>
            <th colspan="5">New&nbsp;Estimates&nbsp;at&nbsp;end&nbsp;of&nbsp;Day...</th>
        </tr>
        <tr>
            <th>1</th><th>2</th><th>3</th><th>4</th><th>5</th>    
        </tr>
    </thead>
    <tbody>
        <tr>
            <td>Lexical analysis</td>
            <td>ADT Position & foldLeft tracking implementation</td>
            <td>JT</td>
            <td>2</td>
            <td>0</td><td>0</td><td>0</td><td>0</td><td>0</td>
        </tr>
                <tr>
            <td rowspan="3">Syntactical analysis</td>
            <td>Add ErrorNode to represent unparsable fragments</td>
            <td>DM</td>
            <td>2</td>
            <td>2</td><td>0</td><td>0</td><td>0</td><td>0</td>
        </tr>
        <tr>
            <td>Implement the Parsing monad for state and error accumulation</td>
            <td>DM</td>
            <td>4</td>
            <td>4</td><td>4</td><td>1</td><td>0</td><td>0</td>
        </tr>
        <tr>
            <td>Recover by synchronisation and collect all errors</td>
            <td>DM</td>
            <td>4</td>
            <td>4</td><td>4</td><td>4</td><td>1</td><td>0</td>
        </tr>
        <tr>
            <td rowspan="4">FINF language</td>
            <td>Define FinfNode AST domain structures</td>
            <td>JT</td>
            <td>2</td>
            <td>0</td><td>0</td><td>0</td><td>0</td><td>0</td>
        </tr>
        <tr>
            <td>Implement TypedExtractors for FINF elements</td>
            <td>JT</td>
            <td>2</td>
            <td>2</td><td>2</td><td>0</td><td>0</td><td>0</td>
        </tr>
        <tr>
            <td>Write decoder rules for expressions & declarations</td>
            <td>JT</td>
            <td>7</td>
            <td>7</td><td>4</td><td>4</td><td>1</td><td>0</td>
        </tr>
        <tr>
            <td>Adapt decoder to resolve right-recursive LL(1) lists</td>
            <td>JT</td>
            <td>2</td>
            <td>2</td><td>2</td><td>2</td><td>2</td><td>0</td>
        </tr>
        <tr>
            <td rowspan="2">Automatic left factoring</td>
            <td>Group alternatives by common prefixes</td>
            <td>MB</td>
            <td>4</td>
            <td>2</td><td>0</td><td>0</td><td>0</td><td>0</td>
        </tr>
        <tr>
            <td>Update conversion logic to apply left factoring</td>
            <td>MB</td>
            <td>5</td>
            <td>5</td><td>5</td><td>2</td><td>0</td><td>0</td>
        </tr>
    </tbody>
</table>

### Sprint Backlog #4

25/08/26

<table>
    <thead>
        <tr>
            <th rowspan="2">Product Backlog Item</th>
            <th rowspan="2">Sprint Task</th>
            <th rowspan="2">Volunteer</th>
            <th rowspan="2">Initial Estimate<br>of Effort</th>
            <th colspan="5">New&nbsp;Estimates&nbsp;at&nbsp;end&nbsp;of&nbsp;Day...</th>
        </tr>
        <tr>
            <th>1</th><th>2</th><th>3</th><th>4</th><th>5</th>    
        </tr>
    </thead>
    <tbody>
        <tr>
            <td rowspan="2">CST to AST conversion</td>
            <td>Change decodeRightRecursiveList logic</td>
            <td>JT</td>
            <td>3</td>
            <td>0</td><td>0</td><td>0</td><td>0</td><td>0</td>
        </tr>
        <tr>
            <td>Use a reference for error handling instead of string</td>
            <td>JT</td>
            <td>2</td>
            <td>2</td><td>2</td><td>0</td><td>0</td><td>0</td>
        </tr>
                <tr>
            <td rowspan="2">Syntactical analysis</td>
            <td>Flatten internal nonterminals out of the CST</td>
            <td>DM</td>
            <td>2</td>
            <td>2</td><td>0</td><td>0</td><td>0</td><td>0</td>
        </tr>
        <tr>
            <td>Move error descriptions into the library</td>
            <td>DM</td>
            <td>3</td>
            <td>3</td><td>3</td><td>1</td><td>0</td><td>0</td>
        </tr>
        <tr>
            <td rowspan="2">FINF language use case</td>
            <td>Implement a CLI to display a demo</td>
            <td>DM</td>
            <td>4</td>
            <td>4</td><td>1</td><td>0</td><td>0</td><td>0</td>
        </tr>
        <tr>
            <td>Write examples of .finf files</td>
            <td>DM</td>
            <td>1</td>
            <td>0</td><td>0</td><td>0</td><td>0</td><td>0</td>
        </tr>
        <tr>
            <td>-</td>
            <td>Expose a single-entry analyzer and cover the pipeline with a test</td>
            <td>DM</td>
            <td>2</td>
            <td>2</td><td>2</td><td>1</td><td>0</td><td>0</td>
        </tr>
        <tr>
            <td>-</td>
            <td>Compile fat jar for release</td>
            <td>MB</td>
            <td>?</td>
            <td>?</td><td>?</td><td>?</td><td>0</td><td>0</td>
        </tr>
    </tbody>
</table>
