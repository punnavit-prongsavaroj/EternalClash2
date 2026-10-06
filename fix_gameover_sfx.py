import re

with open('code/src/main/resources/static/js/app.js', 'r', encoding='utf-8') as f:
    js = f.read()

old_cond = "if (onclickAttr.includes('confirmMarshal') || onclickAttr.includes('rerollMarshal')) {"
new_cond = "if (onclickAttr.includes('confirmMarshal') || onclickAttr.includes('rerollMarshal') || onclickAttr.includes('returnToLobby') || onclickAttr.includes('logout')) {"

js = js.replace(old_cond, new_cond)

with open('code/src/main/resources/static/js/app.js', 'w', encoding='utf-8') as f:
    f.write(js)

print("Game Over buttons SFX fixed")
