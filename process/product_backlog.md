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
            <td>?</td>
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
            <td>?</td>
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
            <td>?</td>
        </tr>
        <tr>
            <td>6</td>
            <td>FINF language use case</td>
            <td>10</td>
            <td>?</td>
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
            <td>?</td>
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
            <td rowspan="2">DSL constructs</td>
            <td>Define types and operations for grammar production elements</td>
            <td>DM</td>
            <td>10</td>
            <td>?</td><td>?</td><td>?</td><td>?</td><td>?</td>
        </tr>
        <tr>
            <td>Infer symbol names from definition</td>
            <td>DM</td>
            <td>4</td>
            <td>?</td><td>?</td><td>?</td><td>?</td><td>?</td>
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
            <td>Lexical analysis</td>
            <td>Implement lexer with the longest-prefix-match algorithm</td>
            <td>JT</td>
            <td>15</td>
            <td>?</td><td>?</td><td>?</td><td>?</td><td>?</td>
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
            <td rowspan="3">Syntactical analysis</td>
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
            <td>Implement parser with the LL(1) algorithm</td>
            <td>DM</td>
            <td>15</td>
            <td>?</td><td>?</td><td>?</td><td>?</td><td>?</td>
        </tr>
        <tr>
            <td>CST to AST conversion constructs</td>
            <td>???</td>
            <td>JT</td>
            <td>12</td>
            <td>?</td><td>?</td><td>?</td><td>?</td><td>?</td>
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
            <td>Include line number and column in tokens</td>
            <td>JT</td>
            <td>2</td>
            <td>?</td><td>?</td><td>?</td><td>?</td><td>?</td>
        </tr>
        <tr>
            <td>Syntactical analysis</td>
            <td>Accumulate parsing errors instead of short-circuiting</td>
            <td>DM</td>
            <td>13</td>
            <td>?</td><td>?</td><td>?</td><td>?</td><td>?</td>
        </tr>
        <tr>
            <td>FINF language use case</td>
            <td>Add converter from CST to AST for the FINF language</td>
            <td>JT</td>
            <td>10</td>
            <td>?</td><td>?</td><td>?</td><td>?</td><td>?</td>
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
            <td></td>
            <td></td>
            <td></td>
            <td></td>
        </tr>
    </tbody>
</table>
