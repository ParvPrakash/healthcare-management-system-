#!/bin/bash
set -e

# ========================================================
# CareFirst Healthcare Management System - Share with Friends
# One-command tunnel launcher via Cloudflare / ngrok
# ========================================================

APP_PORT=8080
HEALTH_URL="http://localhost:${APP_PORT}/health"
TARGET_DIR="target"
APP_LOG="${TARGET_DIR}/app.log"
TUNNEL_LOG="${TARGET_DIR}/tunnel.log"

mkdir -p "$TARGET_DIR"

cleanup() {
    echo ""
    echo "🛑 Shutting down CareFirst sharing session..."
    if [ -n "$TUNNEL_PID" ] && kill -0 "$TUNNEL_PID" 2>/dev/null; then
        kill "$TUNNEL_PID" 2>/dev/null || true
    fi
    if [ -n "$APP_PID" ] && kill -0 "$APP_PID" 2>/dev/null; then
        kill "$APP_PID" 2>/dev/null || true
    fi
    echo "👋 Stopped. Have a great day!"
    exit 0
}

trap cleanup INT TERM EXIT

echo "======================================================="
echo "🏥 CareFirst Healthcare System - Friend Sharing Tunnel"
echo "======================================================="

# 1. Check for tunnel tool
TUNNEL_TOOL=""
if command -v cloudflared &>/dev/null; then
    TUNNEL_TOOL="cloudflared"
elif command -v /opt/homebrew/bin/cloudflared &>/dev/null; then
    TUNNEL_TOOL="/opt/homebrew/bin/cloudflared"
elif command -v ngrok &>/dev/null; then
    TUNNEL_TOOL="ngrok"
fi

if [ -z "$TUNNEL_TOOL" ]; then
    echo "❌ Neither 'cloudflared' nor 'ngrok' was detected on your system."
    echo ""
    echo "👉 Install cloudflared in 5 seconds:"
    echo "   - macOS:   brew install cloudflared"
    echo "   - Linux:   sudo apt install cloudflared (or visit https://pkg.cloudflare.com/)"
    echo "   - Windows: winget install Cloudflare.cloudflared"
    echo ""
    echo "👉 Alternatively, install ngrok from https://ngrok.com/download"
    exit 1
fi

# 2. Check if the app is already running and healthy
APP_PID=""
if curl -s -f -m 2 "$HEALTH_URL" &>/dev/null; then
    echo "✅ Application is already running and healthy on port ${APP_PORT}."
else
    echo "🚀 Starting CareFirst Healthcare Management System (mvn compile exec:java)..."
    mvn compile exec:java > "$APP_LOG" 2>&1 &
    APP_PID=$!

    echo "⏳ Waiting for application to initialize on http://localhost:${APP_PORT}..."
    MAX_ATTEMPTS=45
    ATTEMPT=0
    HEALTHY=0

    while [ $ATTEMPT -lt $MAX_ATTEMPTS ]; do
        sleep 1
        ATTEMPT=$((ATTEMPT + 1))
        if curl -s -f -m 2 "$HEALTH_URL" &>/dev/null; then
            HEALTHY=1
            break
        fi
        printf "."
    done
    echo ""

    if [ $HEALTHY -ne 1 ]; then
        echo "❌ Application failed to become healthy within 45 seconds."
        echo "Check log output: tail -n 50 $APP_LOG"
        exit 1
    fi
    echo "✅ CareFirst app is ready!"
fi

# 3. Start Tunnel
echo "🌐 Launching secure HTTPS tunnel via ${TUNNEL_TOOL}..."
rm -f "$TUNNEL_LOG"

if [[ "$TUNNEL_TOOL" == *"cloudflared"* ]]; then
    $TUNNEL_TOOL tunnel --url "http://localhost:${APP_PORT}" > "$TUNNEL_LOG" 2>&1 &
    TUNNEL_PID=$!

    echo "⏳ Generating temporary public link..."
    PUBLIC_URL=""
    for i in {1..20}; do
        sleep 1
        if [ -f "$TUNNEL_LOG" ]; then
            # Cloudflare tunnel URLs match https://*.trycloudflare.com
            MATCH=$(grep -o -E 'https://[a-zA-Z0-9-]+\.trycloudflare\.com' "$TUNNEL_LOG" | head -n 1 || true)
            if [ -n "$MATCH" ]; then
                PUBLIC_URL="$MATCH"
                break
            fi
        fi
    done

    if [ -z "$PUBLIC_URL" ]; then
        echo "⚠️ Cloudflare tunnel started, but link parsing timed out."
        echo "Check log file directly: cat $TUNNEL_LOG"
    else
        echo ""
        echo "======================================================="
        echo "🎉 YOUR TEMPORARY PUBLIC LINK IS LIVE!"
        echo "======================================================="
        echo ""
        echo "🔗 Share this link with friends:"
        echo "   👉 $PUBLIC_URL"
        echo ""
        echo "🔑 Demo Logins (included on login page):"
        echo "   👑 Admin:   admin@carefirst.com         / Admin@123"
        echo "   🩺 Doctor:  doctor.sarah@carefirst.com  / Doctor@123"
        echo "   👤 Patient: john.doe@example.com        / Patient@123"
        echo ""
        echo "💡 Tips:"
        echo "   - Works from any phone or computer with internet access."
        echo "   - Keep this terminal window open while sharing."
        echo "   - Press [Ctrl + C] anytime to stop the tunnel and app."
        echo "======================================================="
    fi

elif [ "$TUNNEL_TOOL" = "ngrok" ]; then
    ngrok http "${APP_PORT}" --log=stdout > "$TUNNEL_LOG" 2>&1 &
    TUNNEL_PID=$!

    echo "⏳ Fetching ngrok public link..."
    sleep 3
    PUBLIC_URL=$(curl -s http://127.0.0.1:4040/api/tunnels 2>/dev/null | grep -o -E 'https://[a-zA-Z0-9-]+\.ngrok-free\.app' | head -n 1 || true)
    if [ -n "$PUBLIC_URL" ]; then
        echo ""
        echo "======================================================="
        echo "🎉 YOUR TEMPORARY NGROK LINK IS LIVE!"
        echo "======================================================="
        echo "🔗 Share this link with friends:"
        echo "   👉 $PUBLIC_URL"
        echo "======================================================="
    fi
fi

# Keep script running to maintain tunnel until Ctrl+C
echo "📡 Tunnel session active. Press [Ctrl + C] to stop."
wait "$TUNNEL_PID" 2>/dev/null || true
