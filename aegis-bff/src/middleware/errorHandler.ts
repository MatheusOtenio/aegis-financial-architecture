import { Request, Response, NextFunction } from 'express';

export const errorHandler = (err: any, req: Request, res: Response, next: NextFunction) => {
    console.error('[Error]', err);

    // Erros conhecidos do Axios (Backend Core fora, 404, 500)
    if (err.isAxiosError) {
        const status = err.response?.status || 502;
        res.status(status).json({
            error: 'Backend Service Error',
            message: err.message,
            backendData: process.env.NODE_ENV === 'development' ? err.response?.data : undefined
        });
        return;
    }

    // Erro genérico
    res.status(500).json({
        error: 'Internal Server Error',
        message: 'An unexpected error occurred.'
    });
};
