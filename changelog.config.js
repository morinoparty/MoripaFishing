module.exports = {
  disableEmoji: false,
  format: '{type}{scope}: {emoji}{subject}',
  list: ['feat', 'fix', 'wip', 'chore', 'docs', 'refactor', 'perf', 'test'],
  maxMessageLength: 64,
  minMessageLength: 3,
  questions: ['type', 'subject', 'issues', 'lerna'],
  scopes: [],
  types: {
    feat: {
      description: 'Feature     (実装)',
      emoji: '✨',
      value: 'feat'
    },
    fix: {
      description: 'Fix         (修復)',
      emoji: '🐛',
      value: 'fix'
    },
    wip: {
      description: 'WIP         (工事)',
      emoji: '🚧',
      value: 'wip'
    },
    chore: {
      description: 'Chore       (雑務)',
      emoji: '📎',
      value: 'chore'
    },
    docs: {
      description: 'Documents   (文書)',
      emoji: '📓',
      value: 'docs'
    },
    perf: {
      description: 'Performance (改善)',
      emoji: '⚡',
      value: 'perf'
    },
    refactor: {
      description: 'Refactoring (改築)',
      emoji: '💡',
      value: 'refactor'
    },
    test: {
      description: 'Test        (試験)',
      emoji: '💯',
      value: 'test'
    },
    messages: {
      type: 'Select the type of change that you\'re committing:',
      customScope: 'Select the scope this component affects:',
      subject: 'Write a short, imperative mood description of the change:\n',
      body: 'Provide a longer description of the change:\n ',
      breaking: 'List any breaking changes:\n',
      footer: 'Issues this commit closes, e.g #123:',
      confirmCommit: 'The packages that this commit has affected\n',
    },
  }
};