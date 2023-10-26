package net.chrisrichardson.bankingexample.apigateway.apigateway;

import net.chrisrichardson.bankingexample.apigateway.apigateway.proxies.AccountServiceProxy;
import net.chrisrichardson.bankingexample.apigateway.apigateway.proxies.CustomerServiceProxy;
import io.eventuate.examples.tram.sagas.ordersandcustomers.customers.apigateway.AccountWithCustomer;
import net.chrisrichardson.bankingexample.accountservice.common.GetAccountResponse;
import net.chrisrichardson.bankingexample.customerservice.common.CustomerInfo;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import javax.validation.constraints.NotNull;

import java.util.Optional;

import static org.springframework.web.reactive.function.BodyInserters.fromValue;

public class AccountHandlers {

  private AccountServiceProxy accountService;
  private CustomerServiceProxy customerService;

  public AccountHandlers(AccountServiceProxy accountService, CustomerServiceProxy customerService) {
    this.accountService = accountService;
    this.customerService = customerService;
  }

  @NotNull
  public Mono<ServerResponse> getAccountWithCustomer(ServerRequest serverRequest) {
//    throw new RuntimeException("not yet implemented");

    String accountId = serverRequest.pathVariable("accountId");

    Mono<Optional<GetAccountResponse>> accountResponse = accountService.findAccountById(accountId);

    return accountResponse
            .flatMap(maybeAccount -> maybeAccount
                    .map(account -> {
                        Mono<CustomerInfo> customerResponse = customerService.findCustomerById(
                                account.getAccountInfo().getCustomerId());
                        Mono<AccountWithCustomer> accountWithCustomerResponse = customerResponse
                                .map(customer -> new AccountWithCustomer(account, customer));

                        return accountWithCustomerResponse
                                .flatMap(accountWithCustomer -> ServerResponse
                                        .ok()
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .body(fromValue(accountWithCustomer))
                                );
                    })
                    .orElseGet(() -> ServerResponse.notFound().build())
            );
  }

}
