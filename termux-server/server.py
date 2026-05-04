from flask import Flask, request, jsonify
from datetime import datetime
import subprocess
import os
import json
import urllib.request

app = Flask(__name__)

START_TIME = datetime.now().strftime("%Y-%m-%d %H:%M:%S")
LAST_COMMAND = ""

LOCAL_SERVER_VERSION_CODE = 1
LOCAL_SERVER_VERSION_NAME = "1.0.0"

REMOTE_VERSION_URL = "https://raw.githubusercontent.com/Andrei9489/JarvisAccessibility/main/server_version.json"
LOCAL_SERVER_PATH = os.path.abspath(__file__)

def safe_shell(cmd):
    try:
        result = subprocess.check_output(
            cmd,
            shell=True,
            stderr=subprocess.STDOUT,
            timeout=15
        )
        return result.decode("utf-8", errors="ignore")
    except subprocess.CalledProcessError as e:
        return e.output.decode("utf-8", errors="ignore")
    except Exception as e:
        return str(e)

def download_text(url):
    with urllib.request.urlopen(url, timeout=20) as response:
        return response.read().decode("utf-8", errors="ignore")

def get_remote_server_info():
    raw = download_text(REMOTE_VERSION_URL)
    return json.loads(raw)

@app.route("/")
def home():
    return """
    <html>
    <head>
        <title>Jarvis Termux Server</title>
        <style>
            body {
                background: #020817;
                color: #0EA5E9;
                font-family: monospace;
                padding: 30px;
            }
            .panel {
                border: 1px solid #0EA5E9;
                border-radius: 14px;
                padding: 20px;
                margin-bottom: 20px;
                background: #071426;
            }
            h1 { color: #22C55E; }
            code { color: #FACC15; }
        </style>
    </head>
    <body>
        <div class="panel">
            <h1>JARVIS TERMUX SERVER ONLINE</h1>
            <p>Status: active</p>
            <p>Version: """ + LOCAL_SERVER_VERSION_NAME + """</p>
            <p>Endpoints:</p>
            <p><code>/status</code></p>
            <p><code>/command?text=hello</code></p>
            <p><code>/say?text=hello sir</code></p>
            <p><code>/device</code></p>
            <p><code>/update/check</code></p>
            <p><code>/update/install</code></p>
        </div>
    </body>
    </html>
    """

@app.route("/status")
def status():
    return jsonify({
        "status": "online",
        "name": "Jarvis Termux Server",
        "started_at": START_TIME,
        "last_command": LAST_COMMAND,
        "serverVersionCode": LOCAL_SERVER_VERSION_CODE,
        "serverVersionName": LOCAL_SERVER_VERSION_NAME
    })

@app.route("/command", methods=["GET", "POST"])
def command():
    global LAST_COMMAND

    if request.method == "POST":
        data = request.get_json(silent=True) or {}
        text = data.get("text", "")
    else:
        text = request.args.get("text", "")

    text = text.strip()
    LAST_COMMAND = text

    if not text:
        return jsonify({
            "ok": False,
            "error": "No command text provided."
        })

    return jsonify({
        "ok": True,
        "received": text,
        "reply": f"Command received, sir: {text}"
    })

@app.route("/say")
def say():
    text = request.args.get("text", "At your service, sir.").strip()

    if not text:
        text = "At your service, sir."

    output = safe_shell(f'termux-tts-speak "{text}"')

    return jsonify({
        "ok": True,
        "spoken": text,
        "output": output
    })

@app.route("/device")
def device():
    return jsonify({
        "battery": safe_shell("termux-battery-status"),
        "wifi": safe_shell("termux-wifi-connectioninfo"),
        "time": datetime.now().strftime("%Y-%m-%d %H:%M:%S")
    })

@app.route("/update/check")
def update_check():
    try:
        remote = get_remote_server_info()
        remote_code = int(remote.get("serverVersionCode", 0))

        return jsonify({
            "ok": True,
            "localVersionCode": LOCAL_SERVER_VERSION_CODE,
            "localVersionName": LOCAL_SERVER_VERSION_NAME,
            "remoteVersionCode": remote_code,
            "remoteVersionName": remote.get("serverVersionName", ""),
            "updateAvailable": remote_code > LOCAL_SERVER_VERSION_CODE,
            "notes": remote.get("notes", ""),
            "serverUrl": remote.get("serverUrl", "")
        })
    except Exception as e:
        return jsonify({
            "ok": False,
            "error": str(e)
        })

@app.route("/update/install")
def update_install():
    try:
        remote = get_remote_server_info()
        remote_code = int(remote.get("serverVersionCode", 0))
        server_url = remote.get("serverUrl", "")

        if remote_code <= LOCAL_SERVER_VERSION_CODE:
            return jsonify({
                "ok": True,
                "message": "Serverul este deja la zi.",
                "localVersionCode": LOCAL_SERVER_VERSION_CODE
            })

        if not server_url:
            return jsonify({
                "ok": False,
                "error": "serverUrl lipsește în server_version.json"
            })

        new_server = download_text(server_url)

        backup_path = LOCAL_SERVER_PATH + ".bak"
        with open(backup_path, "w", encoding="utf-8") as backup:
            with open(LOCAL_SERVER_PATH, "r", encoding="utf-8") as current:
                backup.write(current.read())

        with open(LOCAL_SERVER_PATH, "w", encoding="utf-8") as target:
            target.write(new_server)

        return jsonify({
            "ok": True,
            "message": "Update server instalat. Repornește serverul Termux.",
            "backup": backup_path,
            "newVersionCode": remote_code,
            "newVersionName": remote.get("serverVersionName", "")
        })

    except Exception as e:
        return jsonify({
            "ok": False,
            "error": str(e)
        })

if __name__ == "__main__":
    print("Jarvis Termux Server running on http://127.0.0.1:8765")
    app.run(host="127.0.0.1", port=8765)
