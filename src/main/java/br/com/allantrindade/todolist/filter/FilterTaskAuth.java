package br.com.allantrindade.todolist.filter;

import java.io.IOException;
import java.util.Base64;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import at.favre.lib.crypto.bcrypt.BCrypt;
import br.com.allantrindade.todolist.user.IUserRespository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class FilterTaskAuth extends OncePerRequestFilter{

    @Autowired
    private IUserRespository userRespository;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
        throws ServletException, IOException {
            var servletPath = request.getServletPath();

            if (servletPath.startsWith("/tasks/")){

                // Pegar autenticação (usuário e senha)
                var authorization = request.getHeader("Authorization");
                
            var authEncoded = authorization.substring("Basic".length()).trim();
            
            byte[] authDecoded = Base64.getDecoder().decode(authEncoded);

            var authString = new String(authDecoded);

            String[] credentials = authString.split(":");
            // ["trindade_", "1q2w3e20"]
            String username = credentials[0];
            String password = credentials[1];

        // Validar usuário
            var user = userRespository.findByUsername(username);
            if (user == null) {
                response.sendError(401);
            }
            else {
                // Validar senha
                var passwordVerifyResult = BCrypt.verifyer().verify(password.toCharArray(), user.getPassword());   

                if (passwordVerifyResult.verified){
                    // Manda idUser pra controller
                    // Segue viagem
                    request.setAttribute("idUser", user.getId());
                    filterChain.doFilter(request, response);
                }
                else {
                    response.sendError(401);
                }
                
            }
            
        }
        else {
            filterChain.doFilter(request, response);
        }
    }
}
    