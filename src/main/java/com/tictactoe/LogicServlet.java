package com.tictactoe;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.List;

@WebServlet(name = "LogicServlet", value = "/logic")
public class LogicServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        // Получение текущей сессии
        HttpSession currentSession = req.getSession();

        // Получение атрибута из текущей сессии - поле
        Field field = extractField(currentSession);

        // получение параметра из строки запроса - индекс ячейки
        int index = getSelectedIndex(req);
        Sign currentSign = field.getField().get(index);

        // проверка ячейки
        if (Sign.EMPTY != currentSign) {
            // редирект без изменений
            getServletContext().getRequestDispatcher("/index.jsp").forward(req, resp);
        }

        // ставим крестик
        field.getField().put(index, Sign.CROSS);
        if (checkWin(resp, currentSession, field)) {
            return;
        }

        // ставим нолик
        int emptyFieldIndex = field.getEmptyFieldIndex();
        if (emptyFieldIndex >= 0) {
            field.getField().put(emptyFieldIndex, Sign.NOUGHT);
            if (checkWin(resp, currentSession, field)) {
                return;
            }
        } else {
            // Добавляем в сессию флаг, который сигнализирует что произошла ничья
            currentSession.setAttribute("draw", true);

            // Считаем список значков
            List<Sign> data = field.getFieldData();

            // Обновляем этот список в сессии
            currentSession.setAttribute("data", data);

            // Шлем редирект
            resp.sendRedirect("/index.jsp");
            return;
        }

        // Считаем список значков
        List<Sign> data = field.getFieldData();

        // Обновление парамтеров в сессии
        currentSession.setAttribute("field", field);
        currentSession.setAttribute("data", data);

        // отправка в ответ редиректа
        resp.sendRedirect("/index.jsp");
    }

    private int getSelectedIndex(HttpServletRequest request) {
        String click = request.getParameter("click");
        boolean isNumeric = click.chars().allMatch(Character::isDigit);
        return isNumeric ? Integer.parseInt(click) : 0;
    }

    private Field extractField(HttpSession currentSession) {
        Object fieldAttribute = currentSession.getAttribute("field");
        if (Field.class != fieldAttribute.getClass()) {
            currentSession.invalidate();
            throw new RuntimeException("Session is broken, try one more time");
        }
        return (Field) fieldAttribute;
    }

    /**
     * Метод проверяет, нет ли трех крестиков/ноликов в ряд.
     * Возвращает true/false
     */
    private boolean checkWin(HttpServletResponse response, HttpSession currentSession, Field field) throws IOException {
        Sign winner = field.checkWin();
        if (Sign.CROSS == winner || Sign.NOUGHT == winner) {
            // Добавляем флаг, который показывает что кто-то победил
            currentSession.setAttribute("winner", winner);

            // Считаем список значков
            List<Sign> data = field.getFieldData();

            // Обновляем этот список в сессии
            currentSession.setAttribute("data", data);

            // Шлем редирект
            response.sendRedirect("/index.jsp");
            return true;
        }
        return false;
    }
}
