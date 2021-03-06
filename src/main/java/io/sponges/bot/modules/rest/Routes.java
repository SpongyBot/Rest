package io.sponges.bot.modules.rest;

import io.sponges.bot.api.module.Module;
import io.sponges.bot.modules.rest.route.GetIndexRoute;
import io.sponges.bot.modules.rest.route.GetStatisticsRoute;
import io.sponges.bot.modules.rest.route.channel.GetChannelRoute;
import io.sponges.bot.modules.rest.route.channel.GetChannelsRoute;
import io.sponges.bot.modules.rest.route.client.GetClientRoute;
import io.sponges.bot.modules.rest.route.client.GetClientsRoute;
import io.sponges.bot.modules.rest.route.command.GetCommandRoute;
import io.sponges.bot.modules.rest.route.command.GetCommandsRoute;
import io.sponges.bot.modules.rest.route.module.GetModuleCommandsRoute;
import io.sponges.bot.modules.rest.route.module.GetModuleRoute;
import io.sponges.bot.modules.rest.route.module.GetModulesRoute;
import io.sponges.bot.modules.rest.route.network.*;
import io.sponges.bot.modules.rest.route.user.GetUserRoute;
import io.sponges.bot.modules.rest.route.user.GetUsersRoute;
import spark.Service;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

class Routes {

    private final Service service;

    Routes(Module module) {
        Authentication authentication = null;
        try {
            authentication = new Authentication(module.getLogger(), new File("authentication.txt"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.service = Service.ignite();
        this.service.port(4568);
        this.service.exception(Exception.class, (exception, request, response) -> {
            exception.printStackTrace();
        });
        Route.setModule(module);
        Route.setAuthentication(authentication);
        register(
                new GetIndexRoute(),
                new GetStatisticsRoute(),

                // module
                new GetModulesRoute(),
                new GetModuleRoute(),
                new GetModuleCommandsRoute(),

                // commands
                new GetCommandsRoute(),
                new GetCommandRoute(),

                // clients
                new GetClientsRoute(),
                new GetClientRoute(),

                // networks
                new GetNetworksRoute(),
                new GetNetworkRoute(),
                new GetNetworkModuleRoute(),
                new GetNetworkModuleDataRoute(),
                new UpdateNetworkModuleDataRoute(),
                new EnableNetworkModuleRoute(),
                new DisableNetworkModuleRoute(),

                // channel
                new GetChannelsRoute(),
                new GetChannelRoute(),

                // user
                new GetUsersRoute(),
                new GetUserRoute()
        );
        this.service.redirect.get("/api", RestModule.API_ROOT);
        this.service.redirect.get(RestModule.API_ROOT, RestModule.API_ROOT + "/");
    }

    private void register(Route... routes) {
        for (Route route : routes) {
            Class<? extends Service> clazz = service.getClass();
            String methodName = route.getMethod().name().toLowerCase();
            try {
                clazz.getMethod(methodName, String.class, spark.Route.class).invoke(service,
                        RestModule.API_ROOT + route.getRoute(), (spark.Route) route::internalExecute);
            } catch (InvocationTargetException | NoSuchMethodException | IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }
}
