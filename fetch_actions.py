import urllib.request
req = urllib.request.Request('http://localhost:8080/api/games/55/players/89/actions', headers={'Accept': 'application/json'}, method='POST')
try:
    with urllib.request.urlopen(req) as response:
        print(response.read().decode('utf-8'))
except urllib.error.HTTPError as e:
    print(f"Action Error {e.code}")
    print(e.read().decode('utf-8'))
