package com.pjt.gestacao.model

import com.pjt.gestacao.R

object InstitutionsData {

    val institutions = listOf(
        Institution(
            id = 1,
            name = "Mãe Coruja",
            type = "Programa Social",
            address = "Localização: Vários locais em Pernambuco",
            image = R.drawable.maecoruja,
            description = "O Mãe Coruja é um programa do Governo de Pernambuco que oferece apoio a gestantes e mães, com foco na saúde, educação e desenvolvimento social. O programa oferece acompanhamento desde a gestação até os primeiros anos de vida da criança.",
            phone = "0800 281 2020",
            site = "http://www.maecoruja.pe.gov.br/"
        ),
        Institution(
            id = 2,
            name = "Casa da Gestante",
            type = "ONG",
            address = "Localização: Recife, PE",
            image = R.drawable.casadagestante,
            description = "A Casa da Gestante é uma ONG que acolhe e oferece suporte a gestantes em situação de vulnerabilidade. O local oferece moradia, alimentação, acompanhamento psicológico e cursos profissionalizantes.",
            phone = "(81) 98765-4321",
            site = "https://www.instagram.com/casadagestanterecife/"
        ),
        Institution(
            id = 3,
            name = "Centro de Parto Rita Barradas",
            type = "Casa de parto",
            address = "Localização: Jaboatão dos Guararapes, PE",
            image = R.drawable.centrodepartorita,
            description = "O Centro de Parto Normal Rita Barradas é uma unidade de saúde especializada em partos normais humanizados. A casa de parto oferece um ambiente acolhedor e seguro para as gestantes, com equipe multidisciplinar e foco no protagonismo da mulher.",
            phone = "(81) 1234-5678",
            site = "https://jaboatao.pe.gov.br/servicos/centro-de-parto-normal-rita-barradas"
        ),
        Institution(
            id = 4,
            name = "Casa Angela - Centro de Parto Humanizado",
            type = "Casa de parto",
            address = "Localização: São Paulo, SP (Exemplo)",
            image = R.drawable.casaangela,
            description = "A Casa Angela é um centro de parto humanizado que oferece assistência integral à gestante, desde o pré-natal até o pós-parto. O local conta com uma equipe de obstetras, enfermeiras obstétricas e doulas, e oferece um ambiente acolhedor e seguro para o parto.",
            phone = "(11) 5548-2321",
            site = "https://www.casaangela.org.br/"
        ),
        Institution(
            id = 5,
            name = "Grupo Mulheres do Brasil",
            type = "ONG",
            address = "Localização: Vários núcleos pelo Brasil",
            image = R.drawable.grupomulheresdobrasil,
            description = "O Grupo Mulheres do Brasil é uma organização que atua em diversas áreas para promover o protagonismo feminino. O grupo oferece projetos de capacitação, empreendedorismo e apoio a mulheres em situação de vulnerabilidade.",
            phone = "contato@grupomulheresdobrasil.org.br",
            site = "https://www.grupomulheresdobrasil.org.br/"
        )
    )
}