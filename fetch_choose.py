import urllib.request
req = urllib.request.Request('http://localhost:8080/api/players/85/marshal-candidates/choose', headers={'Accept': 'application/json'}, method='POST')
try:
    with urllib.request.urlopen(req) as response:
        print(response.read().decode('utf-8'))
except urllib.error.HTTPError as e:
    print(f"Error {e.code}")
    print(e.read().decode('utf-8'))
