import urllib.request
try:
    with urllib.request.urlopen('http://localhost:8080/js/app.js') as response:
        print(f"Status: {response.status}")
        print(response.read()[:100].decode('utf-8'))
except Exception as e:
    print(e)
