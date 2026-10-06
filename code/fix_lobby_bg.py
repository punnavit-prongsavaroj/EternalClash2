import re

with open('src/main/resources/static/css/style.css', 'r', encoding='utf-8', errors='ignore') as f:
    css = f.read()

css = css.replace("background-image: url('../Map/sunny.jpg');", "background-image: url('../Menu/menuBG.jpg');")

with open('src/main/resources/static/css/style.css', 'w', encoding='utf-8') as f:
    f.write(css)
