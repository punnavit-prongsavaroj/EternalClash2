with open('code/src/main/resources/static/js/app.js', 'r', encoding='utf-8') as f:
    js = f.read()

# Update mapping just in case
old_map = "'โจโฉ': 'sungon.mp4',"
new_map = "'โจโฉ': 'josho.mp4',"
js = js.replace(old_map, new_map)

start_idx = js.find("} else if (snapshot.status === 'FINISHED') {")
if start_idx != -1:
    end_idx = js.find("let lastActionMap = {};", start_idx)
    # The end_idx might be 'let lastActionMap' or something else
    # Let's search for function updateGameUI(snapshot) {
    if end_idx == -1:
        end_idx = js.find("function updateGameUI(snapshot) {", start_idx)

    if end_idx != -1:
        # We want to replace from start_idx up to the end of the FINISHED block.
        # The block ends with     }\n}\n\n
        # Let's just find }\n} backwards from end_idx
        while js[end_idx-1] == '\\n' or js[end_idx-1] == '\\r' or js[end_idx-1] == ' ' or js[end_idx-1] == '/':
            end_idx -= 1
            if js[end_idx-1] == '-' or js[end_idx-1] == 'E' or js[end_idx-1] == 'M': # catching // ----- GAME PHASE -----
                end_idx -= 1
        
        # We need a robust way. Let's just use regex with the exact text snippet
