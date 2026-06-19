# Architecture Rules

Never:
- Call APISIX directly from Controller
- Access Repository from Controller

Always:
- Use Service Layer
- Use Repository Abstraction
