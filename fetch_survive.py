import os
files = os.listdir('code/src/main/resources/static/Marshal')
for f in files:
    if 'json' in f:
        with open('code/src/main/resources/static/Marshal/'+f, 'r', encoding='utf-8') as file:
            content = file.read()
            if 'SURVIVE_DESTRUCTION' in content:
                print(f"Found in {f}: {content}")
