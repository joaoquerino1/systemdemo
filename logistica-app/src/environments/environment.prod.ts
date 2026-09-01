export const environment = {
  production: true,
  // In production, the frontend and API are served from the same origin
  // (nginx proxies /api to the backend). Using a relative URL avoids
  // hardcoded domains and works across environments.
  apiUrl: '/api'
};
