with open('code/src/main/resources/static/js/app.js', 'a', encoding='utf-8') as f:
    f.write('''

function closeCommandPanel() {
    selectedCityIdForAction = null;
    document.getElementById('command-panel').style.display = 'none';
}
''')
