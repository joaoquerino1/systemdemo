import { defineRailway, github, postgres, preserve, project, service, volume } from "railway/iac";

export default defineRailway(() => {
  const Postgres = postgres("Postgres", { region: "ams" });
  const postgresVolume3cN7 = volume("postgres-volume-3cN7", { alerts: { usage: { "100": {}, "80": {}, "95": {} } }, allowOnlineResize: true, region: "ams", sizeMB: 500 });

  const api = service("api", {
    source: github("joaoquerino1/systemdemo", { branch: "master", rootDirectory: "logistica-api", checkSuites: false }),
    build: { watchPatterns: ["/logistica-api/**"] },
    replicas: { "ams": 1 },
    env: { CORS_ORIGENS: preserve(), DB_PASSWORD: preserve(), DB_URL: preserve(), DB_USER: preserve(), EMPRESA_CNPJ: preserve(), EMPRESA_NOME: preserve(), JWT_SECRET: preserve(), SPRING_PROFILES_ACTIVE: preserve() },
  });
  const app = service("app", {
    source: github("joaoquerino1/systemdemo", { branch: "master", rootDirectory: "logistica-app", checkSuites: false }),
    build: { watchPatterns: ["/logistica-app/**"] },
    replicas: { "ams": 1 },
    env: { BACKEND_INTERNAL_URL: preserve() },
  });

  return project("systemdemo", {
    resources: [api, app, Postgres, postgresVolume3cN7],
  });
});
