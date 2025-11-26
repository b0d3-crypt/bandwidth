## Mikrotik Bandwidth Monitor

Aplicação full‑stack para monitorar consumo de banda em um roteador MikroTik via SNMP, com backend em Spring Boot e frontend em React + Vite + Tailwind.

---

## 1. Pré‑requisitos

- **Java 17+** (para o backend Spring Boot)
- **Maven 3+**
- **Node.js 18+** e **npm**
- Um **MikroTik RouterOS** (físico ou VM) com:
  - SNMP habilitado (v2c, comunidade `public` ou a que você configurar no backend)
  - Acesso à porta **161/UDP**
  - Winbox (opcional) para configuração das interfaces

---

## 2. Estrutura do projeto

- `backend` (código Java dentro de `src/main/java/com/example/Bandwidth/...`)
- `frontend/` (React + Vite + Tailwind)

---

## 3. Configuração do MikroTik

1. **Habilitar SNMP**

   - Em Winbox: `IP > SNMP`
   - Habilite o serviço, configure a comunidade (por padrão o backend usa `public`).

2. **Conferir interfaces**

   - Em Winbox: `Interfaces`
   - Anote os **nomes exatos** das interfaces que você quer monitorar (ex.: `ether1`, `ether2`, etc.).

3. (Opcional) **VM com duas interfaces**
   - Adicione uma segunda placa de rede na VM (VirtualBox/VMware/qemu).
   - Depois confira no Winbox se ela aparece como nova interface (`ether2`, `ether3`, ...).

---

## 4. Configurar o frontend (IP e interfaces)

Arquivo: `frontend/src/App.jsx`

- **Antes de rodar**, ajuste a constante `MIKROTIK_IP` para o IP/hostname correto do seu MikroTik:

```javascript
const MIKROTIK_IP = "192.168.1.166"; // altere para o IP/hostname do seu MikroTik
```

- No mesmo arquivo você também escolhe os **nomes de interface** que aparecem no seletor:

```jsx
<select
  id="interface"
  value={interfaceName}
  onChange={(e) => {
    setInterfaceName(e.target.value);
    setData([]);
  }}
  ...
>
  <option value="ether1">ether1</option>
  <option value="ether2">ether2</option>
  {/* Adicione aqui outras interfaces, se quiser */}
</select>
```

Certifique‑se de que os nomes (`value="etherX"`) batem exatamente com os nomes das interfaces no MikroTik.

---

## 5. Rodando o backend (Spring Boot)

1. No diretório raiz do projeto (onde está este `README.md`), rode:

```bash
mvn clean install
mvn spring-boot:run
```

2. O backend sobe por padrão em `http://localhost:8080` e expõe o endpoint:

- `POST /api` — recebe `{ "ipAddress": "...", "interfaceName": "..." }` e retorna as taxas em Mbps.

Deixe o backend rodando enquanto usa o frontend.

---

## 6. Rodando o frontend (React + Vite + Tailwind)

1. Em outro terminal, vá para a pasta `frontend`:

```bash
cd frontend
```

2. Instale as dependências (primeira vez):

```bash
npm install
```

3. Inicie o servidor de desenvolvimento:

```bash
npm run dev
```

4. Acesse no navegador:

```text
http://localhost:3000
```

O frontend já está configurado com Tailwind, PostCSS e Vite; não é necessário comando extra para build de CSS.

---

## 7. Fluxo de uso

1. Ajuste `MIKROTIK_IP` em `frontend/src/App.jsx` para o IP do seu MikroTik.
2. Confirme que os nomes das interfaces no `<select>` (`ether1`, `ether2`, etc.) existem no seu MikroTik.
3. Inicie o **backend** (`mvn spring-boot:run`).
4. Inicie o **frontend** (`npm run dev` dentro de `frontend`).
5. No navegador:
   - Informe o IP/hostname (se for diferente do `MIKROTIK_IP` padrão).
   - Escolha a interface desejada.
   - Clique em **Pausar/Retomar** conforme quiser controlar as requisições.

Se os valores aparecerem zerados para alguma interface, verifique:

- Se há tráfego real naquela interface.
- Se o nome da interface está correto no select.
- Se o IP/hostname do MikroTik está correto.

---
