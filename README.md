# fn-recorders

Small lib for recording to atoms+files times of function execution (profiling) or function call results.

## Usage

    ; just pretend we have a function (or several of them):
    (defn long-f []
      (let [wait-for (rand-int 300)]
        (Thread/sleep wait-for)
        (str wait-for "!")))
        
    ; and want to have a time of execution|results of calling them in some files
    (add-file-watch-to-recorded! :times :->csv "times.csv")
    (add-file-watch-to-recorded! :results :->json "results.json")
    
    ; so call it with macro
    ;; first arg is alias for saving (simple type for csv and more composite for json)
    (record-times-for :long-f (long-f))
    (record-results-for {:long-f "meme"} (long-f))
    
All functions called through macro record to files with aliases(axis) so  
`times.csv` content will be like:
    
    :long-f	288

`results.json` content will be like:

    [{"axis":{"long-f":"meme"},"result":"43!"}]
    
Also, we can specify a middleware for results processing, 
if we don't want to save the function's call results as it is:

    (binding [*middleware* [#(str/replace % #"!" "") read-string]]
        (record-results-for {:long-f "meme"} (long-f)))
        
Will produce in `results.json`

    [{"axis":{"long-f":"meme"},"result":43}]
    
    
Also, look to tests/src if you have questions.

## License

Copyright © 2020 Vladislav Shishkov

This program and the accompanying materials are made available under the
terms of the Eclipse Public License 2.0 which is available at
http://www.eclipse.org/legal/epl-2.0.

This Source Code may also be made available under the following Secondary
Licenses when the conditions for such availability set forth in the Eclipse
Public License, v. 2.0 are satisfied: GNU General Public License as published by
the Free Software Foundation, either version 2 of the License, or (at your
option) any later version, with the GNU Classpath Exception which is available
at https://www.gnu.org/software/classpath/license.html.
