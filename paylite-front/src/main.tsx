import React from 'react'
import ReactDOM from 'react-dom/client'
import { BrowserRouter } from 'react-router-dom'
import App from './App'
import keycloak from './auth'
import './styles.css'

async function startApp() {
  const root = document.getElementById('root')

  if (!root) {
    throw new Error('Root element not found')
  }

  try {
    await keycloak.init({
      onLoad: 'login-required',
      pkceMethod: 'S256',
      checkLoginIframe: false,
    })

    ReactDOM.createRoot(root).render(
      <React.StrictMode>
        <BrowserRouter>
          <App />
        </BrowserRouter>
      </React.StrictMode>,
    )
  } catch (error) {
    console.error('Keycloak initialization failed', error)
    root.innerText = 'Login failed. Check that Keycloak is running and the client is configured correctly.'
  }
}

void startApp()
