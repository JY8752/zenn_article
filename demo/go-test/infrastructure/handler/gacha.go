package handler

import (
	"context"
	"net/http"
	"strconv"

	"github.com/JY8752/go-unittest-architecture/controller"
	"github.com/labstack/echo/v4"
)

type Gacha struct {
	e  *echo.Echo
	gc *controller.Gacha
}

func NewGacha(e *echo.Echo, gc *controller.Gacha) *Gacha {
	return &Gacha{e, gc}
}

func (g *Gacha) Register() {
	g.draw()
}

type ErrorResponse struct {
	Message string `json:"message"`
	Err     error  `json:"error"`
}

func (g *Gacha) draw() {
	g.e.POST("/gacha/:gachaId/draw", func(c echo.Context) error {
		gachaId, err := strconv.Atoi(c.Param("gachaId"))
		if err != nil {
			return c.JSON(http.StatusBadRequest, ErrorResponse{
				Message: "gachaId parameter is invalid",
				Err:     err,
			})
		}

		item, err := g.gc.Draw(context.Background(), gachaId)
		if err != nil {
			return c.JSON(http.StatusBadRequest, ErrorResponse{
				Message: "failed to draw gacha",
				Err:     err,
			})
		}

		return c.JSON(http.StatusOK, item)
	})
}
