import urllib.request
req = urllib.request.Request('http://localhost:8080/api/games/50/snapshot?viewerPlayerId=81', headers={'Accept': 'application/json'})
try:
    with urllib.request.urlopen(req) as response:
        print(response.read().decode('utf-8'))
except urllib.error.HTTPError as e:
    print(f"Error {e.code}")
    print(e.read().decode('utf-8'))
