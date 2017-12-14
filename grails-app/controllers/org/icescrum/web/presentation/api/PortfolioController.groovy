/*
 * Copyright (c) 2017 Kagilum SAS
 *
 * This file is part of iceScrum.
 *
 * iceScrum is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License.
 *
 * iceScrum is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with iceScrum.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Authors:
 *
 * Vincent Barrier (vbarrier@kagilum.com)
 * Nicolas Noullet (nnoullet@kagilum.com)
 *
 */

package org.icescrum.web.presentation.api

import grails.converters.JSON
import grails.plugin.springsecurity.annotation.Secured
import org.icescrum.core.domain.Portfolio
import org.icescrum.core.domain.Project
import org.icescrum.core.error.ControllerErrorHandler
import org.icescrum.web.presentation.windows.ProjectController

@Secured('isAuthenticated()')
class PortfolioController implements ControllerErrorHandler {

    def portfolioService
    def springSecurityService

    @Secured('isAuthenticated()')
    def save() {
        def portfolioParams = params.portfolio
        Portfolio portfolio = new Portfolio()
        Portfolio.withTransaction {
            //create each project using the projectController
            ProjectController projectController = new ProjectController()
            def nbProjects = portfolioParams.int('projectsSize')
            def projects = []
            (nbProjects).times {
                def projectParam = portfolioParams.projects."$it"
                if (projectParam.id) {
                    projects << Project.get(projectParam.id) // TODO check that user is PO
                } else {
                    projectController.params.project = projectParam
                    projectController.params.internalCall = true
                    projects << projectController.save()
                }
            }
            bindData(portfolio, portfolioParams, [include: ['fkey', 'name', 'description']])
            def businessOwners = [springSecurityService.currentUser] // TODO take a real list
            def portfolioStakeholders = []                           // TODO take a real list
            portfolioService.save(portfolio, projects, businessOwners, portfolioStakeholders)
            render(status: 201, text: portfolio as JSON, contentType: 'application/json')
        }
    }

    @Secured('businessOwner()')
    def delete(long portfolio) {
        Portfolio _portfolio = Portfolio.withPortfolio(portfolio)
        portfolioService.delete(_portfolio)
        withFormat {
            html {
                render(status: 200, text: [id: portfolio] as JSON)
            }
            json {
                render(status: 204)
            }
        }
    }

    @Secured('isAuthenticated()')
    def available(long portfolio, String property) {
        def result = false
        if (property == 'fkey') {
            result = request.JSON.value && request.JSON.value =~ /^[A-Z0-9]*$/ && (portfolio ? Portfolio.countByFkeyAndId(request.JSON.value, portfolio) : Portfolio.countByFkey(request.JSON.value)) == 0
        }
        render(status: 200, text: [isValid: result, value: request.JSON.value] as JSON, contentType: 'application/json')
    }

    @Secured('isAuthenticated()')
    def add() {
        render(status: 200, template: "dialogs/new")
    }
}