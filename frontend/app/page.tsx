'use client'

// useEffect: roda código quando o componente aparece na tela (ex: buscar dados)
// useState: guarda valores que podem mudar e re-renderiza a tela quando mudam
import { useEffect, useState } from 'react'

// Tipo que descreve o formato de um agente vindo da API
type Agent = {
  id: number
  name: string
  active: boolean   // true = ativo, false = inativo
  role: string | null   // pode não existir, por isso | null
  status: string | null
}

export default function Home() {
  // Lista de agentes que vieram do backend
  const [agents, setAgents] = useState<Agent[]>([])

  // true enquanto o fetch ainda não terminou
  const [loading, setLoading] = useState(true)

  // Guarda a mensagem de erro, começa vazia
  const [error, setError] = useState('')

  // Valor que o usuário digita no campo de nome
  const [newAgentName, setNewAgentName] = useState('')

  // Controla se o formulário de criação está visível ou não
  const [showForm, setShowForm] = useState(false)

  // true enquanto o POST de criação está em andamento
  const [creating, setCreating] = useState(false)

  // useEffect com [] roda UMA ÚNICA VEZ quando a página carrega
  // Sem o [] rodaria infinitamente a cada re-render
  useEffect(() => {
    // fetch é nativo do navegador — não precisa instalar nada
    fetch('http://localhost:8080/agents?page=0&size=50')
      .then(res => res.json())       // converte o texto da resposta em objeto JS
      .then(data => {
        setAgents(data.content)      // a API retorna { content: [...], totalPages: ... }
        setLoading(false)
      })
      .catch(() => {
        setError('Erro ao carregar agentes. Verifique se o backend está rodando.')
        setLoading(false)
      })
  }, [])

  // Função chamada ao clicar em "Criar Agente"
  const handleCreate = async () => {
    // Garante que o nome não está em branco (trim remove espaços extras)
    if (!newAgentName.trim()) return

    setCreating(true)  // ativa o loading do botão
    setError('')       // limpa erro anterior

    try {
      // POST /agents — envia os dados do novo agente para o backend
      const res = await fetch('http://localhost:8080/agents', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' }, // avisa o backend que mandamos JSON
        body: JSON.stringify({ name: newAgentName }),     // converte objeto JS → texto JSON
      })

      // Se o servidor devolveu um status de erro (4xx, 5xx), lança exceção
      if (!res.ok) throw new Error('Falha ao criar agente')

      // Pega o agente recém-criado que o backend devolveu
      const agenteCriado: Agent = await res.json()

      // Adiciona no início da lista sem precisar buscar tudo de novo
      // prev é o valor atual de agents — usamos função para não perder dados
      setAgents(prev => [agenteCriado, ...prev])

      // Reseta o formulário
      setNewAgentName('')
      setShowForm(false)
    } catch {
      setError('Erro ao criar agente. Tente novamente.')
    } finally {
      // finally roda sempre, com sucesso ou erro
      setCreating(false)
    }
  }

  // --- Telas de estado ---

  // Enquanto o fetch não terminou, mostra tela de loading
  if (loading) {
    return (
      <div className="flex items-center justify-center min-h-screen bg-gray-950">
        <p className="text-gray-400 text-lg">Carregando agentes...</p>
      </div>
    )
  }

  // Se deu erro E não tem nenhum agente para mostrar, mostra só o erro
  if (error && agents.length === 0) {
    return (
      <div className="flex items-center justify-center min-h-screen bg-gray-950">
        <p className="text-red-400 text-lg">{error}</p>
      </div>
    )
  }

  // --- Tela principal ---
  return (
    <div className="min-h-screen bg-gray-950 text-white p-8">
      <div className="max-w-5xl mx-auto">

        {/* Cabeçalho com título e botão de novo agente */}
        <div className="flex items-center justify-between mb-8">
          <div>
            <h1 className="text-2xl font-bold text-white">Field Agents</h1>
            {/* Mostra quantos agentes tem na lista */}
            <p className="text-gray-400 text-sm mt-1">{agents.length} agente{agents.length !== 1 ? 's' : ''} cadastrado{agents.length !== 1 ? 's' : ''}</p>
          </div>

          {/* Clicando aqui alterna entre mostrar e esconder o formulário */}
          <button
            onClick={() => setShowForm(!showForm)}
            className="bg-green-600 hover:bg-green-700 text-white px-4 py-2 rounded-lg text-sm font-medium transition-colors"
          >
            {showForm ? 'Cancelar' : '+ Novo Agente'}
          </button>
        </div>

        {/* Formulário — só aparece na tela quando showForm for true */}
        {showForm && (
          <div className="bg-gray-800 border border-gray-700 rounded-lg p-5 mb-6">
            <h2 className="text-base font-semibold mb-4">Criar novo agente</h2>

            <div className="flex gap-3">
              <input
                type="text"
                value={newAgentName}
                onChange={e => setNewAgentName(e.target.value)}
                onKeyDown={e => e.key === 'Enter' && handleCreate()} // Enter também funciona
                placeholder="Nome do agente..."
                className="flex-1 bg-gray-900 border border-gray-600 rounded-lg px-3 py-2 text-sm text-white placeholder-gray-500 focus:outline-none focus:border-green-500"
              />

              <button
                onClick={handleCreate}
                disabled={creating || !newAgentName.trim()} // desativa se criando ou sem nome
                className="bg-green-600 hover:bg-green-700 disabled:opacity-50 disabled:cursor-not-allowed px-5 py-2 rounded-lg text-sm font-medium transition-colors"
              >
                {/* Texto muda conforme o estado */}
                {creating ? 'Criando...' : 'Criar'}
              </button>
            </div>

            {/* Erro do formulário — só aparece se houver erro */}
            {error && <p className="text-red-400 text-xs mt-2">{error}</p>}
          </div>
        )}

        {/* Lista de agentes em grid — 1 coluna no mobile, 2 no tablet, 3 no desktop */}
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
          {/* map percorre o array e transforma cada agente em um card */}
          {agents.map(agent => (
            <div
              key={agent.id}  // key obrigatório em listas — ajuda o React a identificar cada item
              className="bg-gray-800 border border-gray-700 hover:border-gray-500 rounded-lg p-4 transition-colors"
            >
              {/* Linha superior: nome + badge ativo/inativo */}
              <div className="flex items-center justify-between mb-3">
                <span className="font-medium text-white truncate">{agent.name}</span>

                {/* Classe condicional: verde se ativo, vermelho se inativo */}
                <span className={`text-xs px-2 py-0.5 rounded-full shrink-0 ml-2 ${
                  agent.active
                    ? 'bg-green-900 text-green-400'
                    : 'bg-red-900 text-red-400'
                }`}>
                  {agent.active ? 'Ativo' : 'Inativo'}
                </span>
              </div>

              {/* ID em fonte mono (números ficam mais legíveis) */}
              <p className="text-gray-500 text-xs font-mono">ID #{agent.id}</p>

              {/* Role — só renderiza se existir (operador && é "se existir, mostra") */}
              {agent.role && (
                <p className="text-gray-400 text-xs mt-1">{agent.role}</p>
              )}

              {/* Status — mesma lógica do role */}
              {agent.status && (
                <p className="text-gray-500 text-xs mt-0.5 capitalize">{agent.status.replace('_', ' ')}</p>
              )}
            </div>
          ))}
        </div>

        {/* Mensagem quando a lista está vazia e não tem erro */}
        {agents.length === 0 && !error && (
          <p className="text-center text-gray-500 py-16">
            Nenhum agente cadastrado ainda. Crie o primeiro!
          </p>
        )}

      </div>
    </div>
  )
}
