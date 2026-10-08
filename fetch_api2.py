import urllib.request
req = urllib.request.Request('http://localhost:8080/api/games/51/snapshot?viewerPlayerId=82', headers={'Accept': 'application/json'})
try:
    with urllib.request.urlopen(req) as response:
        print(response.read().decode('utf-8'))
except urllib.error.HTTPError as e:
    print(f"Error {e.code}")
    print(e.read().decode('utf-8'))
