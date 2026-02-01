// Define variaveis de ambiente especificas para teste
process.env.USE_MOCKS = 'true';
process.env.PORT = '3001'; // Porta diferente para não colidir se dev server estiver rodando
console.log = jest.fn(); 
